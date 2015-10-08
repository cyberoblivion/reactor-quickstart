/**
 * @author Stephane Maldini
 */

import groovy.transform.CompileStatic
import reactor.bus.EventBus
import reactor.bus.Event;
import reactor.bus.selector.Selectors
import reactor.quickstart.Trade
import reactor.quickstart.TradeServer

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@CompileStatic
void test() {
	CountDownLatch latch
	int totalTrades = 10000000
	long startTime
	long endTime
	double elapsed
	double throughput

	def startTimer = {
		println "Starting throughput test with $totalTrades trades..."
		latch = new CountDownLatch(totalTrades)
		startTime = System.currentTimeMillis()
	}

	def endTimer = {
		latch.await(30, TimeUnit.SECONDS);
		endTime = System.currentTimeMillis()
		elapsed = (endTime - startTime) * 1.0
		throughput = totalTrades / (elapsed / 1000)
		println "Executed ${(int) throughput} trades/sec in $elapsed ms"
	}

	def server = new TradeServer()

	// Use a Reactor to dispatch events using the RingBuffer Dispatcher
	def reactor = EventBus.create()

	def topic = 'trade.execute'

	// For each Trade event, execute that on the server
	reactor.on(Selectors.object(topic)) { Event<Trade> ev ->
		server.execute ev.getData()

		// Since we're async, for this test, use a latch to tell when we're done
		latch.countDown()
	}

	// Start a throughput timer
	startTimer()

	// Publish one event per trade
	for (int i in 0..totalTrades) {

		// Notify the Reactor the next randomly-generated Trade from server is ready to be handled
		reactor.notify topic, Event.wrap(server.nextTrade())
	}

	// Stop throughput timer and output metrics
	endTimer()

	server.stop()
}

test()


