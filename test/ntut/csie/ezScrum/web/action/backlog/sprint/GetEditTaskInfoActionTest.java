package ntut.csie.ezScrum.web.action.backlog.sprint;

import java.io.File;
import java.io.IOException;
import java.util.List;

import ntut.csie.ezScrum.issue.sql.service.core.Configuration;
import ntut.csie.ezScrum.refactoring.manager.ProjectManager;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.test.CreateData.InitialSQL;
import ntut.csie.jcis.resource.core.IProject;
import servletunit.struts.MockStrutsTestCase;

public class GetEditTaskInfoActionTest extends MockStrutsTestCase {
	private CreateProject CP;
	private CreateSprint CS;
	private Configuration configuration;
	private final String ACTION_PATH = "/getEditTaskInfo";
	private IProject project;
	
	public GetEditTaskInfoActionTest(String testName) {
		super(testName);
	}
	
	protected void setUp() throws Exception {
		configuration = new Configuration();
		configuration.setTestMode(true);
		configuration.save();
		
		// 刪除資料庫
		InitialSQL ini = new InitialSQL(configuration);
		ini.exe();
		this.CP = new CreateProject(1);
		this.CP.exeCreate();// 新增一測試專案
		this.project = this.CP.getProjectList().get(0);
		
		this.CS = new CreateSprint(2, this.CP);
		this.CS.exe();
		
		super.setUp();
		// ================ set action info ========================
		setContextDirectory( new File(configuration.getBaseDirPath()+ "/WebContent") );
		setServletConfigFile("/WEB-INF/struts-config.xml");
		setRequestPathInfo( this.ACTION_PATH );
		
		ini = null;
	}
	
	protected void tearDown() throws IOException, Exception {
		InitialSQL ini = new InitialSQL(configuration);
		ini.exe();
		
		ProjectManager projectManager = new ProjectManager();
		projectManager.deleteAllProject();
		projectManager.initialRoleBase(configuration.getDataPath());
		
		configuration.setTestMode(false);
		configuration.save();
		
		super.tearDown();
		
		ini = null;
		projectManager = null;
		this.CP = null;
		this.CS = null;
		configuration = null;
	}
	
	public void testGetEditTaskInfo() throws Exception {
		List<String> idList = this.CS.getSprintIDList();
		int sprintID = Integer.parseInt(idList.get(0));
		int storyCount = 1;
		int storyEst = 2;
		AddStoryToSprint addStoryToSprint = new AddStoryToSprint(storyCount, storyEst, sprintID, this.CP, CreateProductBacklog.TYPE_ESTIMATION);
		addStoryToSprint.exe();
		
		int taskCount = 1;
		int taskEst = 2;
		AddTaskToStory addTaskToStory = new AddTaskToStory(taskCount, taskEst, addStoryToSprint, this.CP);
		addTaskToStory.exe();
		
		CreateProductBacklog createProductBacklog = new CreateProductBacklog(storyCount, this.CP);
		createProductBacklog.exe();
		String taskId = String.valueOf(addTaskToStory.getTasksId().get(0));
		// ================ set request info ========================
		String projectName = this.project.getName();
		request.setHeader("Referer", "?PID=" + projectName);
		addRequestParameter("sprintID", idList.get(0));
		addRequestParameter("issueID", taskId);
		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", configuration.getUserSession());
		// ================  執行 action ==============================
		actionPerform();
		// ================    assert    =============================
		verifyNoActionErrors();
		verifyNoActionMessages();
		//	assert response text
		String expectedTaskName = addTaskToStory.getTasks().get(0).getName();
		String expectedTaskEstimation= addTaskToStory.getTasks().get(0).getEstimate() + "";
		String expectedTaskActualHour = addTaskToStory.getTasks().get(0).getActual() + "";
		String expectedTaskRemains = addTaskToStory.getTasks().get(0).getRemains() + "";
		String expectedTaskNote = addTaskToStory.getTasks().get(0).getNotes();
		
		StringBuilder expectedResponseTest = new StringBuilder();
		expectedResponseTest.append("<EditTask><Task><Id>" + taskId + "</Id>");
		expectedResponseTest.append("<Name>" + expectedTaskName	+ "</Name>");
		expectedResponseTest.append("<Estimate>" + expectedTaskEstimation + "</Estimate>");
		expectedResponseTest.append("<Actual>" + expectedTaskActualHour + "</Actual><Handler></Handler>");
		expectedResponseTest.append("<Remains>" + expectedTaskRemains + "</Remains><Partners></Partners>");
		expectedResponseTest.append("<Notes>" + expectedTaskNote + "</Notes></Task></EditTask>");
		
		String actualResponseText = response.getWriterBuffer().toString();
		System.out.println("result = " + actualResponseText);
		assertEquals(expectedResponseTest.toString(), actualResponseText);
		
	}
}
