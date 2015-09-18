package spring.osgi.example.item.service.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import spring.osgi.example.item.service.Item;
import spring.osgi.example.item.service.ItemService;

import java.util.List;

/**
 * Created by nico.
 */
@Transactional
public class ItemServiceImpl implements ItemService {

    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);

    private final ItemRepository itemRepository;

    public ItemServiceImpl(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Override
    @Transactional
    public void create(Item item) {
        logger.debug("Asking to save item {}", item);
        itemRepository.save(item);
        logger.info("Item '{}' saved", item.getTitle());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Item> findAll() {
        logger.trace("Asking all items");
        return itemRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Item find(String title) {
        logger.trace("Asking item '{}'", title);
        return itemRepository.findOne(title);
    }

    @Override
    @Transactional
    public void delete(String title) {
        logger.debug("Asking to save item {}", title);
        itemRepository.delete(title);
        logger.info("Item '{}' deleted", title);
    }
}