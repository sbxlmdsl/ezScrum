package ntut.csie.ezScrum.restful.mobilel.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ntut.csie.ezScrum.issue.sql.service.core.Configuration;
import ntut.csie.ezScrum.issue.sql.service.core.InitialSQL;
import ntut.csie.ezScrum.restful.mobile.service.ProjectWebService;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.web.dataObject.ProjectObject;
import ntut.csie.jcis.account.core.LogonException;

public class ProjectWebServiceTest {
	private Configuration mConfig;
	private ProjectWebService mProjectWebService;
	private CreateProject mCP;

	@Before
	public void setUp() {
		mConfig = new Configuration();
		mConfig.setTestMode(true);
		mConfig.save();

		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// ============= release ==============
		ini = null;
	}

	@After
	public void tearDown() {
		// 初始化 SQL
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();

		// 讓 config 回到 Production 模式
		mConfig.setTestMode(false);
		mConfig.save();

		// ============= release ==============
		ini = null;
		mCP = null;
		mConfig = null;
		mProjectWebService = null;
	}

	@Test
	public void testGetProjectList() throws LogonException {
		String username = "admin";
		String userpwd = "admin";
		// 新增 Project
		int ProjectCount = 5;
		mCP = new CreateProject(ProjectCount);
		mCP.exeCreateForDb();

		mProjectWebService = new ProjectWebService(username, userpwd);
		assertEquals(ProjectCount, mProjectWebService.getAllProjects().size());
	}

	@Test
	public void testGetRESTFulResponseString() throws LogonException,
			JSONException {
		String username = "admin";
		String password = "admin";
		String demoDate = "No Plan!";
		// 新增Project
		int projectCount = 5;
		mCP = new CreateProject(projectCount);
		mCP.exeCreateForDb();
		mProjectWebService = new ProjectWebService(username, password);
		String response = mProjectWebService.getRESTFulResponseString();

		JSONObject object = new JSONObject(response);
		JSONArray actualInfos = object.getJSONArray("Projects");
		ArrayList<ProjectObject> projects = mProjectWebService.getAllProjects();
		for (int i = 0; i < projectCount; i++) {
			JSONObject actualInfo = actualInfos.getJSONObject(i).getJSONObject(
					"Project");
			assertEquals(projects.get(i).getName(), actualInfo.getString("id"));
			assertEquals(projects.get(i).getDisplayName(),
					actualInfo.getString("Name"));
			assertEquals(projects.get(i).getComment(),
					actualInfo.getString("Comment"));
			assertEquals(projects.get(i).getManager(),
					actualInfo.getString("ProjectManager"));
			assertEquals(demoDate, actualInfo.getString("DemoDate"));
		}
	}
}
