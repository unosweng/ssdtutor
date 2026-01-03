
package view;

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

import db.DBSearchAnsweredQuestions;
import db.DBSendQuestion;
import db.DBShowRecentQuestions;

@SuppressWarnings("unused")
public class SampleViewer2 {
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
   public SampleViewer2() {

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
      gdStyledTextCodeFr.verticalIndent = 2;
      gdStyledTextCodeFr.horizontalIndent = 2;
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

      StyledText desc = new StyledText(compositeReqGroup, SWT.BORDER);
      GridData gd_styledText2 = new GridData(SWT.FILL, SWT.FILL, true, true, COLUMN_2_OF_2, 1);
      gd_styledText2.heightHint = 100;
      desc.setLayoutData(gd_styledText2);

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
      new Label(compositeBtnRequest, SWT.NONE);
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

      // Composite composite = new Composite(parent, SWT.NONE);
      // composite.setLayout(new GridLayout(5, false));
      // composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      //
      // Button btnNewButton = new Button(composite, SWT.NONE);
      // btnNewButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
      // btnNewButton.setText("New Button1");
      //
      // Button btnNewButton_1 = new Button(composite, SWT.NONE);
      // btnNewButton_1.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      // btnNewButton_1.setText("New Buttonnnnnnnn2");
      //
      // Button btnNewButton_2 = new Button(composite, SWT.NONE);
      // btnNewButton_2.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
      // btnNewButton_2.setText("New Buttonnnnnnnn3");

      // GridData firstData2 = new GridData(SWT.FILL, SWT.FILL, true, false);
      // firstData2.heightHint = 400;
      // GridData gData2 = new GridData(SWT.FILL, SWT.FILL, true, false);
      // gData2.heightHint = 400;
      //
      // Text text = null;
      // Label label = null;
      // StyledText styledText = null;
      // GridData secondData = new GridData(SWT.FILL, SWT.FILL, true, true);
      // secondData.heightHint = 50;
      // parent.setLayout(new FormLayout());
      //
      // TabFolder tabFolder = new TabFolder(parent, SWT.NONE);
      // FormData fd_tabFolder = new FormData();
      // fd_tabFolder.bottom = new FormAttachment(0, 300);
      // fd_tabFolder.right = new FormAttachment(0, 225);
      // fd_tabFolder.top = new FormAttachment(0);
      // fd_tabFolder.left = new FormAttachment(0);
      // tabFolder.setLayoutData(fd_tabFolder);
      //
      // //
      // // 1st Group
      // //
      //
      // TabItem tbItemRequest = new TabItem(tabFolder, SWT.NONE);
      // tbItemRequest.setText("Request Form");
      //
      // Group groupFirst = new Group(tabFolder, SWT.NONE);
      // tbItemRequest.setControl(groupFirst);
      // groupFirst.setText("Request Form");
      // groupFirst.setLayout(new GridLayout(1, false));
      // GridData firstData = new GridData(SWT.FILL, SWT.FILL, true, false);
      // firstData.heightHint = 400;
      // groupFirst.setLayoutData(firstData);
      //
      // ScrolledComposite scrollFirst = new ScrolledComposite(groupFirst, SWT.V_SCROLL);
      // scrollFirst.setLayout(new GridLayout(1, false));
      // scrollFirst.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      //
      // // int szGridLayout = 4;
      // // int column1Of2 = szGridLayout - 1;
      // // int column2Of2 = szGridLayout - 3;
      // // int column1Of1 = szGridLayout;
      //
      // // int szGridLayout = 5;
      // // int column1Of2 = szGridLayout - 1;
      // // int column2Of2 = szGridLayout - 4;
      // // int column1Of3 = szGridLayout - 1;
      // // int column2Of3 = szGridLayout - 2;
      // // int column3Of3 = szGridLayout - 2;
      //
      // Composite firstContent = new Composite(scrollFirst, SWT.NONE);
      // firstContent.setLayout(new GridLayout(5, false));
      // firstContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
      //
      // Button btnSearch = new Button(firstContent, SWT.CENTER);
      // btnSearch.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 5, 1));
      // btnSearch.setText("btn1");
      // new Label(firstContent, SWT.NONE);
      // new Label(firstContent, SWT.NONE);
      // new Label(firstContent, SWT.NONE);
      // new Label(firstContent, SWT.NONE);
      // new Label(firstContent, SWT.NONE);
      //
      // // Label lblStudentID = new Label(firstContent, SWT.NONE);
      // // lblStudentID.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, column1Of2, 1));
      // // lblStudentID.setText("Student ID");
      //
      // scrollFirst.setContent(firstContent);
      //
      // TabItem tabItem_2 = new TabItem(tabFolder, SWT.NONE);
      // tabItem_2.setText("New Item");
      //
      // //
      // // 2nd Group
      // //
      //
      // TabItem tabItem_1 = new TabItem(tabFolder, SWT.NONE);
      // tabItem_1.setText("New Item");
      //
      // ScrolledComposite firstScroll2 = new ScrolledComposite(tabFolder, SWT.V_SCROLL);
      // tabItem_1.setControl(firstScroll2);
      // firstScroll2.setLayout(new GridLayout(1, false));
      //
      // Composite firstContent2 = new Composite(firstScroll2, SWT.NONE);
      // firstContent2.setLayout(new GridLayout(2, false));
      // firstContent2.setLayoutData(gData2);
      //
      // label = new Label(firstContent2, SWT.NONE);
      // label.setText("New Label1");
      //
      // text = new Text(firstContent2, SWT.BORDER);
      // text.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      //
      // label = new Label(firstContent2, SWT.NONE);
      // label.setText("New Label2");
      //
      // text = new Text(firstContent2, SWT.BORDER);
      // text.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      //
      // label = new Label(firstContent2, SWT.NONE);
      // label.setText("New Label2");
      //
      // text = new Text(firstContent2, SWT.BORDER);
      // text.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      //
      // label = new Label(firstContent2, SWT.NONE);
      // label.setText("New Label3");
      //
      // text = new Text(firstContent2, SWT.BORDER);
      // text.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      //
      // label = new Label(firstContent2, SWT.NONE);
      // label.setText("New Label4");
      //
      // text = new Text(firstContent2, SWT.BORDER);
      // text.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      //
      // label = new Label(firstContent2, SWT.NONE);
      // label.setText("New Label5");
      //
      // text = new Text(firstContent2, SWT.BORDER);
      // text.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      //
      // label = new Label(firstContent2, SWT.NONE);
      // label.setText("New Label6");
      //
      // text = new Text(firstContent2, SWT.BORDER);
      // text.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      //
      // label = new Label(firstContent2, SWT.NONE);
      // label.setText("New Label7");
      //
      // text = new Text(firstContent2, SWT.BORDER);
      // text.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
      //
      // label = new Label(firstContent2, SWT.NONE);
      // label.setText("New Label7");
      //
      // styledText = new StyledText(firstContent2, SWT.BORDER);
      // GridData gd_styledText = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
      // gd_styledText.heightHint = 500;
      // styledText.setLayoutData(gd_styledText);
      //
      // firstScroll2.setContent(firstContent2);
      // firstScroll2.setExpandHorizontal(true);
      // firstScroll2.setExpandVertical(true);
      // firstScroll2.setMinSize(firstContent2.computeSize(SWT.DEFAULT, SWT.DEFAULT));
      //
      // TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
      // tabItem.setText("New Item");
      //
      // text_1 = new Text(tabFolder, SWT.BORDER | SWT.MULTI);
      // tabItem.setControl(text_1);
   }
}