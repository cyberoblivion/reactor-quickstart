package reactor.quickstart.spring.repository;

import org.springframework.data.repository.CrudRepository;
import reactor.quickstart.spring.domain.Client;

/**
 * @author Jon Brisbin
 */
public interface ClientRepository extends CrudRepository<Client, Long> {
}
