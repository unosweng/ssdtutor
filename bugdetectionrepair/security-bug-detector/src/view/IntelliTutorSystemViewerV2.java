
package view;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

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

      Label lblStudentVCS = new Label(compositeReqGroup, SWT.NONE);
      lblStudentVCS.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, COLUMN_1_OF_2, 1));
      lblStudentVCS.setText("Student VCS");

      studentVCS = new Text(compositeReqGroup, SWT.BORDER);
      studentVCS.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, COLUMN_2_OF_2, 1));

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
      // lblSpaceComBtnReq.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
      lblSpaceComBtnReq.setText("                          ");

      Button btnSend = new Button(compositeBtnRequest, SWT.NONE);
      btnSend.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
      btnSend.setText("Send Question");
      btnSend.addListener(SWT.Selection, new Listener() {
         public void handleEvent(Event event) {
            switch (event.type) {
            case SWT.Selection:
               System.out.println("[DBG] Send Question");
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