package it.thecrawlers;

import static org.junit.Assert.*;
import it.thecrawlers.crawler.dao.ItemDAO;
import it.thecrawlers.model.Item;
import it.thecrawlers.parser.ItemReviewsPageParser;

import java.io.IOException;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class DAOTest {

	private static final Logger logger = LoggerFactory.getLogger(ItemReviewsPageParser.class);

	@Autowired
	ItemDAO itemDao;
	
	@Test
	public void testParser() throws IOException {
		Item item = new Item();
		item.setId("1");
		item.setName("name");
		itemDao.save(item );
		
		List<Item> list = itemDao.list();
		assertEquals(1, list.size());
	}

}
