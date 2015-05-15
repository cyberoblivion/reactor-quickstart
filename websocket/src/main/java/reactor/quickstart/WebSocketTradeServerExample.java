package reactor.quickstart;

import reactor.Environment;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.bus.selector.Selector;
import reactor.bus.selector.Selectors;
import reactor.io.codec.StandardCodecs;
import reactor.io.net.NetStreams;
import reactor.rx.Streams;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Stephane Maldini
 */
public class WebSocketTradeServerExample {

	private static       int    totalTrades = 10000000;
	private static CountDownLatch latch;
	private static long           startTime;

	public static void main(String[] args) throws Exception {
		Environment env = Environment.initializeIfEmpty();

		final TradeServer server = new TradeServer();

		// Use a Reactor to dispatch events using the high-speed Dispatcher
		final EventBus serverReactor = EventBus.create(env);

		// Create a single key and Selector for efficiency
		final Selector tradeExecute = Selectors.object("trade.execute");

		// For each Trade event, execute that on the server and notify connected clients
		// because each client that connects links to the serverReactor
		serverReactor.on(tradeExecute, (Event<Trade> ev) -> {
			server.execute(ev.getData());

			// Since we're async, for this test, use a latch to tell when we're done
			latch.countDown();
		});

		NetStreams
				.<String, String>httpServer(spec ->
								spec.codec(StandardCodecs.STRING_CODEC).listen(3000)
				).
				ws("/", channel -> {
					System.out.println("Connected a websocket client: " + channel.remoteAddress());

					return Streams.wrap(serverReactor.on(tradeExecute)).
							map(ev -> ev.getData()).
							cast(Trade.class).
							window(1000).
							flatMap(s ->
											channel
													.writeWith(s.
															reduce(0f, (prev, trade) -> (trade.getPrice() + prev) / 2).
															map(Object::toString))
													.log("after-write")
							);
				}).
				start().
				await();

		System.out.println("Connect websocket clients now (waiting for 10 seconds).\n"
				+ "Open websocket/src/main/webapp/ws.html in a browser...");
		Thread.sleep(10000);

		// Start a throughput timer
		startTimer();

		// Publish one event per trade
		for (int i = 0; i < totalTrades; i++) {
			// Pull next randomly-generated Trade from server
			Trade t = server.nextTrade();

			// Notify the Reactor the event is ready to be handled
			serverReactor.notify(tradeExecute.getObject(), Event.wrap(t));
		}

		// Stop throughput timer and output metrics
		endTimer();

		server.stop();

	}

	private static void startTimer() {
		System.out.println("Starting throughput test with " + totalTrades + " trades...");
		latch = new CountDownLatch(totalTrades);
		startTime = System.currentTimeMillis();
	}

	private static void endTimer() throws InterruptedException {
		latch.await(30, TimeUnit.SECONDS);
		long endTime = System.currentTimeMillis();
		double elapsed = endTime - startTime;
		double throughput = totalTrades / (elapsed / 1000);

		System.out.println("Executed " + ((int) throughput) + " trades/sec in " + ((int) elapsed) + "ms");
	}

}
