package spring.osgi.example.item.service.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.osgi.example.item.service.Item;

/**
 * Created by nico.
 */
public interface ItemRepository extends JpaRepository<Item, String> {

}
