package reactor.quickstart.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import reactor.Environment;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.quickstart.Trade;
import reactor.quickstart.TradeServer;
import reactor.spring.context.config.EnableReactor;

import static reactor.bus.selector.Selectors.$;

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
	public EventBus reactor(Environment env, TradeServer tradeServer) {
		Logger log = LoggerFactory.getLogger("trade.server");
		EventBus ev = EventBus.create(env);

		// Wire an event handler to execute trades
		ev.on($("trade.execute"), (Event<Trade> e) -> {
			tradeServer.execute(e.getData());
			log.info("Executed trade: {}", e.getData());
		});

		return ev;
	}

	public static void main(String... args) {
		SpringApplication.run(SpringTradeServerExample.class, args);
	}

}
