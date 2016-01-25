package ntut.csie.ezScrum.web.action.report;

import java.io.File;
import java.io.IOException;

import ntut.csie.ezScrum.issue.sql.service.core.Configuration;
import ntut.csie.ezScrum.issue.sql.service.core.InitialSQL;
import ntut.csie.ezScrum.test.CreateData.AddStoryToSprint;
import ntut.csie.ezScrum.test.CreateData.AddTaskToStory;
import ntut.csie.ezScrum.test.CreateData.CreateProductBacklog;
import ntut.csie.ezScrum.test.CreateData.CreateProject;
import ntut.csie.ezScrum.test.CreateData.CreateSprint;
import ntut.csie.ezScrum.web.dataObject.ProjectObject;
import ntut.csie.ezScrum.web.dataObject.TaskObject;
import ntut.csie.ezScrum.web.logic.SprintBacklogLogic;
import servletunit.struts.MockStrutsTestCase;

public class ResetTaskActionTest extends MockStrutsTestCase {
	private CreateProject mCP;
	private CreateSprint mCS;
	private AddStoryToSprint mASTS;
	private AddTaskToStory mATTS;
	private Configuration mConfig;

	public ResetTaskActionTest(String testMethod) {
		super(testMethod);
	}

	// 目前 setUp 設定的情境為︰產生1個Project、產生1個Sprint、Sprint產生1個Story、每個Story設定點數1點
	// 將Story加入到Sprint內、每個 Story 產生1個1點的 Tasks 並且正確加入
	protected void setUp() throws Exception {
		mConfig = new Configuration();
		mConfig.setTestMode(true);
		mConfig.save();
		
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe(); // 初始化 SQL

		mCP = new CreateProject(1);
		mCP.exeCreateForDb(); // 新增一測試專案

		mCS = new CreateSprint(1, mCP);
		mCS.exe(); // 新增一個 Sprint

		mASTS = new AddStoryToSprint(1, 1, mCS, mCP, CreateProductBacklog.COLUMN_TYPE_EST);
		mASTS.exe(); // 新增五筆 Stories 到 Sprints 內，並設計 Sprint 的 Story 點數總和為 10

		mATTS = new AddTaskToStory(1, 1, mASTS, mCP);
		mATTS.exe(); // 新增兩筆 Task 到各個 Stories 內

		super.setUp();

		setContextDirectory(new File(mConfig.getBaseDirPath() + "/WebContent"));	// 設定讀取的 struts-config 檔案路徑
		setServletConfigFile("/WEB-INF/struts-config.xml");
		setRequestPathInfo("/resetTask");

		// ============= release ==============
		ini = null;
	}

	protected void tearDown() throws IOException, Exception {
		InitialSQL ini = new InitialSQL(mConfig);
		ini.exe();
		
		mConfig.setTestMode(false);
		mConfig.save();

		super.tearDown();

		// ============= release ==============
		ini = null;
		mCP = null;
		mCS = null;
		mASTS = null;
		mATTS = null;
		mConfig = null;
	}

	// 測試Task ReOpen時的狀況
	public void testResetTask() {
		// ================ set initial data =======================
		ProjectObject project = mCP.getAllProjects().get(0);
		TaskObject task = mATTS.getTasks().get(0); // 取得Task資訊
		Long taskId = task.getId();
		SprintBacklogLogic sprintBacklogLogic = new SprintBacklogLogic(project, mCS.getSprintsId().get(0));

		// ================== set parameter info ====================
		addRequestParameter("Id", String.valueOf(taskId)); // 取得第一筆 Task ID
		addRequestParameter("Name", task.getName());
		addRequestParameter("Notes", task.getNotes());
		addRequestParameter("ChangeDate", "");

		// ================ set session info ========================
		request.getSession().setAttribute("UserSession", mConfig.getUserSession());
		request.getSession().setAttribute("Project", project);
		request.setHeader("Referer", "?PID=" + project.getName()); // SessionManager 會對URL的參數作分析 ,未帶入此參數無法存入session

		// ================ 執行 action ==============================
		// 先設定Task為assigned的狀態 在測試
		sprintBacklogLogic.checkOutTask(task.getId(), task.getName(), mConfig.USER_ID, "", task.getNotes(), "");
		actionPerform();
		// 驗證回傳 path
		verifyNoActionErrors();
		// 驗證是否正確存入資料
		task = TaskObject.get(taskId); // 重新取得Task資訊
		StringBuilder expectedResponseText = new StringBuilder();
		expectedResponseText.append("{")
							.append("\"success\":true,")
							.append("\"Issue\":{")
							.append("\"Id\":").append(String.valueOf(taskId)).append(",")
							.append("\"Link\":\"").append("\",")
							.append("\"Name\":\"").append(task.getName()).append("\",")
							.append("\"Handler\":\"").append("").append("\",")
							.append("\"Partners\":\"").append(task.getPartnersUsername()).append("\"}")
							.append("}");
		String actualResponseText = response.getWriterBuffer().toString();
		assertEquals(expectedResponseText.toString(), actualResponseText);
		assertEquals(TaskObject.STATUS_UNCHECK, task.getStatus()); // 判斷Task狀態是不是回到Uncheck了

		// ============= release ==============
		project = null;
		sprintBacklogLogic = null;
		task = null;
	}
}