
package view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import concurrency.scheduler.PeriodicalPoolingExecutor;
import db.DBSearchAnsweredQuestions;
import db.DBSendQuestion;
import db.DBSendResponse;
import db.DBShowOutdatedUnansweredQuestions;
import db.DBShowRecentQuestions;
import db.DBVoteDown;
import db.DBVoteUp;
import util.CheckMisuseNicad;
import util.UtilAST;
import visitor.FindMisuseECBDetector;
import util.CodeFragments;

public class IntelliTutorSystemViewerV2 {
	private Text studentID;
	private Text studentName;
	private Text studentVCS;
	private Text responseId;
	private Text requestID;
	private Text reviewerName;

	final int SZ_GRID_LAYOUT = 5;
	final int COLUMN_1_OF_2 = 1;
	final int COLUMN_2_OF_2 = 4;

	final int POOLING_EXE_PERIOD = 30;
	final int BUTTON_WIDTH = 250;
	final int SPACE_WIDTH = 150;

	final int BUTTON_WIDTH_VOTE = 370;
	final int SPACE_WIDTH_VOTE = 30;

	@Inject
	EPartService epartService;

	@Inject
	public IntelliTutorSystemViewerV2() {
	}

	@PostConstruct
	public void postConstruct(Composite parent) {
		// *
		// *
		// * Groups
		// *
		// *
		TabFolder tabFolder = new TabFolder(parent, SWT.NONE);

		// *
		// *
		// * 1st Group
		// *
		// *
		TabItem tbItemReqGroup = new TabItem(tabFolder, SWT.NONE);
		tbItemReqGroup.setText("Request Form");
		Group groupRequest = new Group(tabFolder, SWT.NONE);
		tbItemReqGroup.setControl(groupRequest);
		groupRequest.setText("Request Form");
		groupRequest.setLayout(new GridLayout(1, false));
		GridData gdGroupReq = new GridData(SWT.FILL, SWT.FILL, true, false);
		gdGroupReq.heightHint = 400;
		groupRequest.setLayoutData(gdGroupReq);

		ScrolledComposite scrollReqGroup = new ScrolledComposite(groupRequest, SWT.V_SCROLL);
		scrollReqGroup.setLayout(new GridLayout(1, false));
		scrollReqGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite compositeReqGroup = new Composite(scrollReqGroup, SWT.NONE);
		compositeReqGroup.setLayout(new GridLayout(SZ_GRID_LAYOUT, false));
		compositeReqGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label lblStudentID = new Label(compositeReqGroup, SWT.NONE);
		lblStudentID.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, COLUMN_1_OF_2, 1));
		lblStudentID.setText("Student ID");

		studentID = new Text(compositeReqGroup, SWT.BORDER);
		studentID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMN_2_OF_2, 1));

		Label lblStudentName = new Label(compositeReqGroup, SWT.NONE);
		lblStudentName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, COLUMN_1_OF_2, 1));
		lblStudentName.setText("Student Name");

		studentName = new Text(compositeReqGroup, SWT.BORDER);
		studentName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMN_2_OF_2, 1));

		//		Label lblStudentVCS = new Label(compositeReqGroup, SWT.NONE);
		//		lblStudentVCS.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, COLUMN_1_OF_2, 1));
		//		lblStudentVCS.setText("Student VCS");
		//
		//		studentVCS = new Text(compositeReqGroup, SWT.BORDER);
		//		studentVCS.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMN_2_OF_2, 1));

		Label lblCodeFragments = new Label(compositeReqGroup, SWT.NONE);
		lblCodeFragments.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, COLUMN_1_OF_2, 1));
		lblCodeFragments.setText("Code Fragments");

		StyledText codeFragments = new StyledText(compositeReqGroup, SWT.BORDER | SWT.FULL_SELECTION | SWT.WRAP | SWT.SCROLL_LINE | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gdStyledTextCodeFr = new GridData(SWT.FILL, SWT.FILL, true, true, COLUMN_2_OF_2, 1);
		gdStyledTextCodeFr.heightHint = 100;
		codeFragments.setLayoutData(gdStyledTextCodeFr);

		Label lblErrType = new Label(compositeReqGroup, SWT.NONE);
		lblErrType.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, COLUMN_1_OF_2, 1));
		lblErrType.setText("Error Type");

		Combo errType = new Combo(compositeReqGroup, SWT.READ_ONLY);
		errType.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMN_2_OF_2, 1));
		String items[] = { "Compile Error", "Runtime Error", "Others" };
		errType.setItems(items);
		errType.select(0);

		Label lblThreshold = new Label(compositeReqGroup, SWT.NONE);
		lblThreshold.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, COLUMN_1_OF_2, 1));
		lblThreshold.setText("Threshold");

		Combo threshold = new Combo(compositeReqGroup, SWT.READ_ONLY);
		threshold.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMN_2_OF_2, 1));
		String thresholds[] = { "100%", "95%", "90%", "85%", "80%", "75%", "70%", "65%", "60%", "55%", "50%", "45%", "40%", "35%", "30%", "25%", "20%", "15%", "10%" };
		threshold.setItems(thresholds);
		threshold.select(6);

		Label lblDesc = new Label(compositeReqGroup, SWT.NONE);
		lblDesc.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, COLUMN_1_OF_2, 1));
		lblDesc.setText("Description");

		StyledText desc = new StyledText(compositeReqGroup, SWT.BORDER | SWT.FULL_SELECTION | SWT.WRAP | SWT.SCROLL_LINE | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gdStyledTextDesc = new GridData(SWT.FILL, SWT.FILL, true, true, COLUMN_2_OF_2, 1);
		gdStyledTextDesc.heightHint = 100;
		desc.setLayoutData(gdStyledTextDesc);

		// *
		// * Buttons
		// *
		Composite compositeBtnRequest = new Composite(compositeReqGroup, SWT.NONE);
		compositeBtnRequest.setLayout(new GridLayout(5, false));
		compositeBtnRequest.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));

		Label lblSpaceComBtnReq = new Label(compositeBtnRequest, SWT.NONE);
		lblSpaceComBtnReq.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		lblSpaceComBtnReq.setText("                          ");
		Button btnSend = new Button(compositeBtnRequest, SWT.NONE);
		btnSend.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnSend.setText("Find Code Examples");
		btnSend.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Selection:
					URL url;
					try {
						long startTime = System.currentTimeMillis();
						url = new URL("http://104.131.172.9:8080/back-end/question/add");
						String code =  codeFragments.getText().toString();
						CodeFragments codeFragment = new CodeFragments();
						CheckMisuseNicad misuseNicad = new CheckMisuseNicad();
                        String misuse = misuseNicad.execute(epartService);
						String s1 = codeFragment.getCodeFragments(code, misuse.toString());
						StringBuilder sb = new StringBuilder();
						String payload="{\"username\": \"" + studentName.getText() + "\",\"student_id\": \"" + studentID.getText() + "\",\"question\": \"" + desc.getText() + "\",\"misuse\": \"" + misuse.toString() + "\",\"threshold\": \"" + threshold.getText() + "\",\"codeFragment\": \"" + s1 + "\"}";
						System.out.println(payload);
						HttpURLConnection connection = (HttpURLConnection) url.openConnection();
						connection.setDoInput(true);
						connection.setDoOutput(true);
						connection.setRequestMethod("POST");
						connection.setRequestProperty("Accept", "application/json");
						connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
						OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), "UTF-8");
						writer.write(payload);
						writer.close();
						BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
						String line;
						StringBuffer jsonString = new StringBuffer();
						while ((line = br.readLine()) != null) {
							jsonString.append(line);
							jsonString.append("\n");
						}
						br.close();
						connection.disconnect();
						sb.append(jsonString.toString());
						String script ="<script src=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.9.0/highlight.min.js\">\r\n"
								+ "        </script>\r\n"
								+ "        <script>\r\n"
								+ "          hljs.initHighlightingOnLoad();\r\n"
								+ "        \r\n"
								+ "        </script>\r\n"
								+ "        <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/highlight.js/9.15.6/styles/default.min.css\"><script language=\"JavaScript\">" +
										"function ShowHide(divId) {" +
										"if(document.getElementById(divId).style.display == 'none') {" +
										"document.getElementById(divId).style.display='block';" +
										"} else {" +
										"document.getElementById(divId).style.display = 'none';" +
										"}" +
										"}" +
										"</script>";
						String finalText = sb.toString() + script ;
						System.out.println("Connection complete");
						MPart findPart = epartService.findPart(NiCadViewer.VIEW_ID);
						if (findPart != null && findPart.getObject() instanceof NiCadViewer) {
							((NiCadViewer) findPart.getObject()).clear();
							((NiCadViewer) findPart.getObject()).setText(finalText);
						}
						long diff = System.currentTimeMillis() - startTime;
						System.out.println("Time in miliseconds: " + diff);
						System.out.println("Time in seconds: " + diff / 1000);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}


					DBSendQuestion.proc(studentID.getText(), studentName.getText(), studentVCS.getText(), //
							codeFragments.getText(), items[errType.getSelectionIndex()], desc.getText());
					break;
				}
			}
		});

		Button btnSearch = new Button(compositeBtnRequest, SWT.NONE);
		btnSearch.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnSearch.setText("Search Answered Questions");
		btnSearch.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Selection:
					System.out.println("[DBG] Search Answered Questions");
					DBSearchAnsweredQuestions.proc(studentID.getText(), codeFragments.getText());
					break;
				}
			}
		});

		Button btnShowAll = new Button(compositeBtnRequest, SWT.NONE);
		btnShowAll.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		btnShowAll.setText("Show Recent 50 Questions");
		btnShowAll.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Selection:
					System.out.println("[DBG] Show Recent 50 Questions");
					DBShowRecentQuestions.proc();
					break;
				}
			}
		});

		scrollReqGroup.setContent(compositeReqGroup);
		scrollReqGroup.setExpandHorizontal(true);
		scrollReqGroup.setExpandVertical(true);
		scrollReqGroup.setMinSize(compositeReqGroup.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		// *
		// *
		// * 2nd Group
		// *
		// *
		TabItem tbItemVotingGroup = new TabItem(tabFolder, SWT.NONE);
		tbItemVotingGroup.setText("Voting");
		Group groupVoting = new Group(tabFolder, SWT.NONE);
		tbItemVotingGroup.setControl(groupVoting);
		groupVoting.setText("Voting");
		groupVoting.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		groupVoting.setLayout(new GridLayout(SZ_GRID_LAYOUT, false));

		Label lblResponseId = new Label(groupVoting, SWT.NONE);
		lblResponseId.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, COLUMN_1_OF_2, 1));
		lblResponseId.setText("Response Id");

		responseId = new Text(groupVoting, SWT.BORDER);
		responseId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMN_2_OF_2, 1));

		Composite compositeBtnVoting = new Composite(groupVoting, SWT.NONE);
		GridLayout gl_compositeBtnVoting = new GridLayout(5, false);
		compositeBtnVoting.setLayout(gl_compositeBtnVoting);
		compositeBtnVoting.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 5, 1));

		Label lblSpaceComBtnVot = new Label(compositeBtnVoting, SWT.NONE);
		// lblSpaceComBtnVot.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true, false, 1, 1));
		lblSpaceComBtnVot.setText("                 ");

		Button btnVoteUp = new Button(compositeBtnVoting, SWT.NONE);
		btnVoteUp.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		btnVoteUp.setText("Vote Up");
		btnVoteUp.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Selection:
					System.out.println("[DBG] Vote Up");
					DBVoteUp.proc(responseId.getText());
					break;
				}
			}
		});

		Button btnVoteDn = new Button(compositeBtnVoting, SWT.NONE);
		btnVoteDn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		btnVoteDn.setText("Vote Down");
		btnVoteDn.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Selection:
					System.out.println("[DBG] Vote Down");
					DBVoteDown.proc(responseId.getText());
					break;
				}
			}
		});

		// *
		// *
		// * 3rd Group
		// *
		// *
		TabItem tbItemResponse = new TabItem(tabFolder, SWT.NONE);
		tbItemResponse.setText("Response Form");
		Group groupResponse = new Group(tabFolder, SWT.NONE);
		tbItemResponse.setControl(groupResponse);
		groupResponse.setText("Response Form");
		groupResponse.setLayout(new GridLayout(1, false));
		groupResponse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		ScrolledComposite scrollResponse = new ScrolledComposite(groupResponse, SWT.V_SCROLL);
		scrollResponse.setLayout(new GridLayout(1, false));
		scrollResponse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Composite compositeResponse = new Composite(scrollResponse, SWT.NONE);
		compositeResponse.setLayout(new GridLayout(SZ_GRID_LAYOUT, false));
		compositeResponse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label lblRequestID = new Label(compositeResponse, SWT.NONE);
		lblRequestID.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, COLUMN_1_OF_2, 1));
		lblRequestID.setText("Request ID");

		requestID = new Text(compositeResponse, SWT.BORDER);
		requestID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMN_2_OF_2, 1));

		Label lblReviewerName = new Label(compositeResponse, SWT.NONE);
		lblReviewerName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, COLUMN_1_OF_2, 1));
		lblReviewerName.setText("Reviewer Name");

		reviewerName = new Text(compositeResponse, SWT.BORDER);
		reviewerName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMN_2_OF_2, 1));

		Label lblSolution = new Label(compositeResponse, SWT.NONE);
		lblSolution.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, COLUMN_1_OF_2, 1));
		lblSolution.setText("Solution");

		StyledText solution = new StyledText(compositeReqGroup, SWT.BORDER | SWT.FULL_SELECTION | SWT.WRAP | SWT.SCROLL_LINE | SWT.V_SCROLL | SWT.H_SCROLL);
		GridData gdStyledText = new GridData(SWT.FILL, SWT.FILL, true, true, COLUMN_2_OF_2, 1);
		gdStyledText.heightHint = 100;
		solution.setLayoutData(gdStyledText);

		Composite compositeBtnResp = new Composite(compositeResponse, SWT.NONE);
		GridLayout glComBtnResp = new GridLayout(5, false);
		glComBtnResp.verticalSpacing = 0;
		glComBtnResp.marginHeight = 0;
		compositeBtnResp.setLayout(glComBtnResp);
		compositeBtnResp.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, false, false, SZ_GRID_LAYOUT, 1));

		Label lblSpaceComBtnResp = new Label(compositeResponse, SWT.NONE);
		// lblSpaceComBtnResp.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		lblSpaceComBtnResp.setText("");

		Button btnShowUnansweredQuestions = new Button(compositeResponse, SWT.NONE);
		btnShowUnansweredQuestions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		btnShowUnansweredQuestions.setText("Show 50 Outdated Unanswered Questions");
		btnShowUnansweredQuestions.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Selection:
					System.out.println("[DBG] Show 50 Outdated Unanswered Questions");
					DBShowOutdatedUnansweredQuestions.proc();
					break;
				}
			}
		});

		Button btnResponse = new Button(compositeResponse, SWT.NONE);
		btnResponse.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		btnResponse.setText("Send Response");
		new Label(compositeResponse, SWT.NONE);
		new Label(compositeResponse, SWT.NONE);
		btnResponse.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.Selection:
					System.out.println("[DBG] Send Response");
					DBSendResponse.proc(requestID.getText(), reviewerName.getText(), solution.getText());
					break;
				}
			}
		});

		scrollResponse.setContent(compositeResponse);
		scrollResponse.setExpandHorizontal(true);
		scrollResponse.setExpandVertical(true);
		scrollResponse.setMinSize(compositeResponse.computeSize(SWT.DEFAULT, SWT.DEFAULT));

		// *
		// *
		// * Notification
		// *
		// *
		ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
		exec.scheduleAtFixedRate(new PeriodicalPoolingExecutor(), 0, POOLING_EXE_PERIOD, TimeUnit.SECONDS);
	}
}
//
//
//
//
//
//
//
//
//