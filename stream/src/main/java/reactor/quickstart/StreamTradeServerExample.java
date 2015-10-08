package reactor.quickstart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.rx.broadcast.Broadcaster;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @author Jon Brisbin
 * @author Stephane Maldini
 */
public class StreamTradeServerExample {

	private static final Logger LOG         = LoggerFactory.getLogger(StreamTradeServerExample.class);
	private static       int    totalTrades = 10000000;

	private static long startTime;

	public static void main(String[] args) throws InterruptedException {
		final TradeServer server = new TradeServer();
		final CountDownLatch latch = new CountDownLatch(totalTrades);

		// Rather than handling Trades as events, each Trade is accessible via Stream.
		Broadcaster<Trade> trades = Broadcaster.create();

		// We compose an action to turn a Trade into an Order by calling server.execute(Trade).
		trades.map(server::execute)
		      .consume(o -> latch.countDown());

		// Start a throughput timer.
		startTimer();

		// Publish one event per trade.
		for (int i = 0; i < totalTrades; i++) {
			// Pull next randomly-generated Trade from server into the Stream,
			Trade trade = server.nextTrade();
			// Notify the Stream this Trade is ready to be executed
			trades.onNext(trade);
		}

		// Wait for all trades to pass through
		latch.await(30, TimeUnit.SECONDS);

		// Stop throughput timer and output metrics.
		endTimer();

		server.stop();
	}

	private static void startTimer() {
		LOG.info("Starting throughput test with {} trades...", totalTrades);
		startTime = System.currentTimeMillis();
	}

	private static void endTimer() throws InterruptedException {
		long endTime = System.currentTimeMillis();
		double elapsed = endTime - startTime;
		double throughput = totalTrades / (elapsed / 1000);

		LOG.info("Executed {} trades/sec in {}ms", (int) throughput, (int) elapsed);
	}

}
