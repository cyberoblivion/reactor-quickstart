package reactor.quickstart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.Environment;
import reactor.bus.Event;
import reactor.bus.EventBus;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static reactor.bus.selector.Selectors.$;

/**
 * @author Jon Brisbin
 */
public class TradeServerExample {

	private static final Logger LOG         = LoggerFactory.getLogger(TradeServerExample.class);
	private static       int    totalTrades = 10000000;

	private static CountDownLatch latch;
	private static long           startTime;

	public static void main(String[] args) throws InterruptedException {
		Environment env = new Environment();
		final TradeServer server = new TradeServer();

		// Use a Reactor to dispatch events using the default Dispatcher
		Reactor reactor = EventBus.create(env);

		String topic = "trade.execute";

		// For each Trade event, execute that on the server
		reactor.on($(topic), (Event<Trade> ev) -> {
			server.execute(ev.getData());

			// Since we're async, for this test, use a latch to tell when we're done
			latch.countDown();
		});

		// Start a throughput timer
		startTimer();

		// Publish one event per trade
		for (int i = 0; i < totalTrades; i++) {
			// Pull next randomly-generated Trade from server
			Trade t = server.nextTrade();

			// Notify the Reactor the event is ready to be handled
			reactor.notify(topic, Event.wrap(t));
		}

		// Stop throughput timer and output metrics
		endTimer();

		server.stop();
	}

	private static void startTimer() {
		LOG.info("Starting throughput test with {} trades...", totalTrades);
		latch = new CountDownLatch(totalTrades);
		startTime = System.currentTimeMillis();
	}

	private static void endTimer() throws InterruptedException {
		latch.await(30, TimeUnit.SECONDS);
		long endTime = System.currentTimeMillis();
		double elapsed = endTime - startTime;
		double throughput = totalTrades / (elapsed / 1000);

		LOG.info("Executed {} trades/sec in {}ms", (int) throughput, (int) elapsed);
	}

}
