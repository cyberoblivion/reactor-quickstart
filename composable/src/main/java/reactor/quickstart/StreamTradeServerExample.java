package reactor.quickstart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.Environment;
import reactor.core.composable.Deferred;
import reactor.core.composable.Stream;
import reactor.core.composable.spec.Streams;

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
		Environment env = new Environment();
		final TradeServer server = new TradeServer();
		final CountDownLatch latch = new CountDownLatch(totalTrades);

		// Rather than handling Trades as events, each Trade is accessible via Stream.
		Deferred<Trade, Stream<Trade>> trades = Streams.defer(env);

		// We compose an action to turn a Trade into an Order by calling server.execute(Trade).
		Stream<Order> orders = trades.compose()
		                             .map(server::execute)
		                             .consume(o -> latch.countDown());

		// Start a throughput timer.
		startTimer();

		// Publish one event per trade.
		for (int i = 0; i < totalTrades; i++) {
			// Pull next randomly-generated Trade from server into the Composable,
			Trade trade = server.nextTrade();
			// Notify the Composable this Trade is ready to be executed
			trades.accept(trade);
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
