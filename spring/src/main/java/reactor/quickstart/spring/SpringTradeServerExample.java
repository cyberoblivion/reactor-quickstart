package reactor.quickstart.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.core.Environment;
import reactor.core.Reactor;
import reactor.event.Event;
import reactor.event.EventBus;
import reactor.quickstart.Trade;
import reactor.quickstart.TradeServer;
import reactor.spring.context.config.EnableReactor;

import static reactor.event.selector.Selectors.$;

/**
 * @author Jon Brisbin
 */
@Configuration
@EnableAutoConfiguration
@ComponentScan
@EnableReactor
public class SpringTradeServerExample {

	@Bean
	public TradeServer tradeServer() {
		return new TradeServer();
	}

	@Bean
	public Reactor reactor(Environment env, TradeServer tradeServer) {
		Logger log = LoggerFactory.getLogger("trade.server");
		Reactor r = EventBus.create(env);

		// Wire an event handler to execute trades
		r.on($("trade.execute"), (Event<Trade> ev) -> {
			tradeServer.execute(ev.getData());
			log.info("Executed trade: {}", ev.getData());
		});

		return r;
	}

	public static void main(String... args) {
		SpringApplication.run(SpringTradeServerExample.class, args);
	}

}
