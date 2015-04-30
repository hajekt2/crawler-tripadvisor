package it.thecrawlers;

import static org.junit.Assert.assertEquals;
import it.thecrawlers.crawler.dao.ItemDAO;
import it.thecrawlers.model.Item;
import it.thecrawlers.parser.ItemReviewsPageParser;

import java.io.IOException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Iterables;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/applicationContext.xml" })
public class DAOTest {

	private static final Logger logger = LoggerFactory.getLogger(ItemReviewsPageParser.class);

	@Autowired
	ItemDAO itemDao;
	
	@Test
	@Transactional
	public void testSave() throws IOException {
		Item item = new Item();
		item.setId("1");
		item.setName("name");
		itemDao.save(item );
		
		Iterable<Item> list = itemDao.findAll();
		assertEquals(1, Iterables.size(list));
	}

}
