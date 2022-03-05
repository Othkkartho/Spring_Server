package server.user.sqlservice.updatable;

import org.junit.Before;
import org.junit.Test;
import server.user.sqlservice.SqlNotFoundException;
import server.user.sqlservice.SqlUpdateFailureException;
import server.user.sqlservice.UpdatableSqlRegistry;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class AbstractUpdatableSqlRegistryTest {
	UpdatableSqlRegistry sqlRegistry;
	
	@Before
	public void setUp() {
		sqlRegistry = createUpdatableSqlRegistry();
		sqlRegistry.registerSql("KEY1", "SQL1");
		sqlRegistry.registerSql("KEY2", "SQL2");
		sqlRegistry.registerSql("KEY3", "SQL3");
	}
	
	abstract protected UpdatableSqlRegistry createUpdatableSqlRegistry();

	@Test
	public void find() {
		checkFind("SQL1", "SQL2", "SQL3");
	}
	
	@Test(expected= SqlNotFoundException.class)
	public void unknownKey() {
		sqlRegistry.findSql("SQL9999!@#$");
	}
	
	protected void checkFind(String expected1, String expected2, String expected3) {
		assertThat(sqlRegistry.findSql("KEY1")).isEqualTo(expected1);
		assertThat(sqlRegistry.findSql("KEY2")).isEqualTo(expected2);
		assertThat(sqlRegistry.findSql("KEY3")).isEqualTo(expected3);
	}
	
	@Test
	public void updateSingle() {
		sqlRegistry.updateSql("KEY2", "Modified2");
		
		checkFind("SQL1", "Modified2", "SQL3");
	}
	
	@Test
	public void updateMulti() {
		Map<String, String> sqlmap = new HashMap<String, String>();
		sqlmap.put("KEY1", "Modified1");
		sqlmap.put("KEY3", "Modified3");
		
		sqlRegistry.updateSql(sqlmap);
		
		checkFind("Modified1", "SQL2", "Modified3");
	}
	
	@Test(expected= SqlUpdateFailureException.class)
	public void updateWithNotExistingKey() {
		sqlRegistry.updateSql("SQL9999!@#$", "Modified2");
	}
}
