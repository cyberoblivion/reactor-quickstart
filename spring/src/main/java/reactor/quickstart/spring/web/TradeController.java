package reactor.quickstart.spring.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.event.Event;
import reactor.quickstart.TradeServer;
import reactor.quickstart.spring.domain.Client;
import reactor.quickstart.spring.repository.ClientRepository;

/**
 * @author Jon Brisbin
 */
@RestController
public class TradeController {

	private final ClientRepository clients;
	private final Reactor          reactor;
	private final TradeServer      tradeServer;

	@Autowired
	public TradeController(ClientRepository clients,
	                       Reactor reactor,
	                       TradeServer tradeServer) {
		this.clients = clients;
		this.reactor = reactor;
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

		reactor.notify("trade.execute", Event.wrap(tradeServer.nextTrade()));

		// Update trade count
		cl = clients.save(cl.setTradeCount(cl.getTradeCount() + 1));

		// Return result
		return "Hello " + cl.getName() + "! You now have " + cl.getTradeCount() + " trades.";
	}

}
