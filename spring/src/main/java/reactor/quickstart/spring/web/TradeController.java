package reactor.quickstart.spring.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.bus.Event;
import reactor.bus.EventBus;
import reactor.quickstart.TradeServer;
import reactor.quickstart.spring.domain.Client;
import reactor.quickstart.spring.repository.ClientRepository;

/**
 * @author Jon Brisbin
 */
@RestController
public class TradeController {

	private final ClientRepository clients;
	private final EventBus         eventBus;
	private final TradeServer      tradeServer;

	@Autowired
	public TradeController(ClientRepository clients,
	                       EventBus eventBus,
	                       TradeServer tradeServer) {
		this.clients = clients;
		this.eventBus = eventBus;
		this.tradeServer = tradeServer;
	}

	@RequestMapping(value = "/", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public Iterable<Client> listClients() {
		return clients.findAll();
	}

	@RequestMapping(value = "/{clientId}", method = RequestMethod.POST, produces = "text/plain")
	@ResponseBody
	public String trade(@PathVariable Long clientId) {
		// Retrieve client by id
		Client cl = clients.findOne(clientId);
		if (null == cl) {
			throw new IllegalArgumentException("No Client found for id " + clientId);
		}

		eventBus.notify("trade.execute", Event.wrap(tradeServer.nextTrade()));

		// Update trade count
		cl = clients.save(cl.setTradeCount(cl.getTradeCount() + 1));

		// Return result
		return "Hello " + cl.getName() + "! You now have " + cl.getTradeCount() + " trades.";
	}

}
