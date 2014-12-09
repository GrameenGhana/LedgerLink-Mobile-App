package org.applab.digitizingdata;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.fontutils.RobotoTextStyleExtractor;
import org.applab.digitizingdata.fontutils.TypefaceManager;
import org.applab.digitizingdata.helpers.CustomGenderSpinnerListener;
import org.applab.digitizingdata.helpers.LongTaskRunner;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MemberRepo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 7/15/13.
 */
public class AddMemberActivity extends SherlockActivity {
    Member selectedMember;
    private int selectedMemberId;
    private boolean successAlertDialogShown = false;
    private boolean selectedFinishButton = false;
    private int meetingId;
    private MemberRepo repo;
    private Meeting targetMeeting;
    private boolean isEditAction;

    private TextView viewClicked;
    private int mYear;
    private int mMonth;
    private int mDay;

    Spinner cboAMMemberNo;
    private TextView txtAMMLoanNextRepaymentDate;
    private TextView txtAMMLoanNumber;
    boolean isGettingStartedMode = false; //flags whether we are in wizard mode

    //Event that is raised when the date has been set
    private final DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            updateDisplay();
        }
    };


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeActivity();
    }

    //This method initializes this activity
    //It is overiden in GSW Add member activity so as to load the relevant layout
    void initializeActivity() {

        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        if (getIntent().hasExtra("_meetingId")) {
            meetingId = getIntent().getIntExtra("_meetingId", 0);
        }
        if (getIntent().hasExtra("_id")) {
            this.selectedMemberId = getIntent().getIntExtra("_id", 0);
        }
        if (getIntent().hasExtra("_isEditAction")) {
            this.isEditAction = getIntent().getBooleanExtra("_isEditAction", false);
        }

        MeetingRepo meetingRepo = new MeetingRepo(getApplicationContext());
        targetMeeting = meetingRepo.getMeetingById(meetingId);
        inflateCustomActionBar();
        // END_INCLUDE (inflate_set_custom_view)
        //if in getting started wizard.. use the getting started layout
        //else use the default layout
        setContentView(R.layout.activity_add_member);

        txtAMMLoanNextRepaymentDate = (TextView) findViewById(R.id.txtAMMLoanNextRepaymentDate);
        txtAMMLoanNumber = (TextView) findViewById(R.id.txtAMMOutstandingLoanNumber);

        //Setup the Spinner Items
        Spinner cboGender = (Spinner) findViewById(R.id.cboAMGender);
        String[] genderList = new String[]{"Male", "Female"};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, genderList)

        {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
                ((TextView) v).setTypeface(externalFont);

                // ((TextView) v).setTextAppearance(getApplicationContext(), R.style.RegularText);

                return v;
            }


            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
                ((TextView) v).setTypeface(externalFont);

                return v;
            }

        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboGender.setAdapter(adapter);
        cboGender.setOnItemSelectedListener(new CustomGenderSpinnerListener());

        //Make the spinner selectable
        cboGender.setFocusable(true);
        cboGender.setFocusableInTouchMode(true);
        cboGender.setClickable(true);
        cboAMMemberNo = (Spinner) findViewById(R.id.cboAMMemberNo);
        clearDataFields();
        if (isEditAction) {
            repo = new MemberRepo(getApplicationContext());
            selectedMember = repo.getMemberById(selectedMemberId);
            populateDataFields(selectedMember);
        }

        // For Middle start details
        if (!isEditAction) {

            // Hide comments & set heading
            TextView lblAMMiddleCycleInformationHeading = (TextView) findViewById(R.id.lblAMMiddleCycleInformationHeading);
            lblAMMiddleCycleInformationHeading.setText("If cycle has started, and your group has held one or more meetings already, enter current totals for member so far.");

            TextView lblAMMiddleCycleSavingsCorrectionLabel = (TextView) findViewById(R.id.lblAMMiddleCycleSavingsCorrectionLabel);
            lblAMMiddleCycleSavingsCorrectionLabel.setVisibility(View.GONE);

            TextView lblAMMiddleCycleSavings = (TextView) findViewById(R.id.lblAMMiddleCycleSavings);
            lblAMMiddleCycleSavings.setVisibility(View.GONE);
//            EditText txtAMMiddleCycleSavingsCorrection = (EditText) findViewById(R.id.txtAMMiddleCycleSavingsCorrection);
//            txtAMMiddleCycleSavingsCorrection.setVisibility(View.GONE);

            TextView lblAMMiddleCycleSavingCorrectionCommentLabel = (TextView) findViewById(R.id.lblAMMiddleCycleSavingCorrectionCommentLabel);
            lblAMMiddleCycleSavingCorrectionCommentLabel.setVisibility(View.GONE);

            EditText txtAMMiddleCycleSavingsCorrectionComment = (EditText) findViewById(R.id.txtAMMiddleCycleSavingsCorrectionComment);
            txtAMMiddleCycleSavingsCorrectionComment.setVisibility(View.GONE);

            TextView lblAMMiddleCycleLoansCorrectionLabel = (TextView) findViewById(R.id.lblAMMiddleCycleLoansCorrectionLabel);
            lblAMMiddleCycleLoansCorrectionLabel.setVisibility(View.GONE);

            EditText txtAMMiddleCycleLoansCorrectionComment = (EditText) findViewById(R.id.txtAMMiddleCycleLoansCorrectionComment);
            txtAMMiddleCycleLoansCorrectionComment.setVisibility(View.GONE);

            TextView lblAMMiddleCycleLoansCorrectionCommentLabel = (TextView) findViewById(R.id.lblAMMiddleCycleLoansCorrectionCommentLabel);
            lblAMMiddleCycleLoansCorrectionCommentLabel.setVisibility(View.GONE);

            TextView lblAMMiddleCycleLoans = (TextView) findViewById(R.id.lblAMMiddleCycleLoans);
            lblAMMiddleCycleLoans.setVisibility(View.GONE);

            //Default next repayment date to a month from now
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.MONTH, 1);
            txtAMMLoanNextRepaymentDate.setText(Utils.formatDate(cal.getTime(), "dd-MMM-yyyy"));

            mYear = cal.get(Calendar.YEAR);
            mMonth = cal.get(Calendar.MONTH);
            mDay = cal.get(Calendar.DAY_OF_MONTH);


        }

        txtAMMLoanNextRepaymentDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // The Event Handler should handle both startDate and endDate
                Date nextRepaymentDate = Utils.stringToDate(txtAMMLoanNextRepaymentDate.getText().toString(), "dd-MMM-yyyy");
                Calendar cal = Calendar.getInstance();
                cal.setTime(nextRepaymentDate);
                mYear = cal.get(Calendar.YEAR);
                mMonth = cal.get(Calendar.MONTH);
                mDay = cal.get(Calendar.DAY_OF_MONTH);

                viewClicked = (TextView) view;
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddMemberActivity.this, mDateSetListener, mYear, mMonth, mDay);
                datePickerDialog.setTitle("Set the next repayment date");
                datePickerDialog.show();
            }
        });

        //To provide formatting for phone numbers
        final EditText txtAMPhoneNo = (EditText) findViewById(R.id.txtAMPhoneNo);
        Utils.setAsPhoneNumberInput(txtAMPhoneNo);
    }


    void showMiddleStartCycleValues(Member member) {
        //loads the Middle start cycle values for this member

        if (isGettingStartedMode) return; //this code shouldnt run in GSW mode hence this check

        //Load the middle start values
        TextView lblAMMiddleCycleInformationHeading = (TextView) findViewById(R.id.lblAMMiddleCycleInformationHeading);
        TextView lblAMMiddleCycleSavings = (TextView) findViewById(R.id.lblAMMiddleCycleSavings);
        TextView lblAMMiddleCycleLoans = (TextView) findViewById(R.id.lblAMMiddleCycleLoans);

        txtAMMLoanNumber.setText(String.valueOf(member.getOutstandingLoanNumberOnSetup()));

        lblAMMiddleCycleSavings.setText(String.format("%,.0f %s", member.getSavingsOnSetup(), getResources().getString(R.string.operating_currency)));
        lblAMMiddleCycleLoans.setText(String.format("%,.0f %s", member.getOutstandingLoanOnSetup(), getResources().getString(R.string.operating_currency)));

        // populate the next repayment date
        if (member.getDateOfFirstRepayment() != null) {
            txtAMMLoanNextRepaymentDate.setText(Utils.formatDate(member.getDateOfFirstRepayment(), "dd-MMM-yyyy"));
        } else {
            txtAMMLoanNextRepaymentDate.setText("none");
            txtAMMLoanNextRepaymentDate.setTextColor(getResources().getColor(R.color.ledger_link_light_blue));
        }

        // Process comments if any
        EditText txtAMMiddleCycleSavingsComment = (EditText) findViewById(R.id.txtAMMiddleCycleSavingsCorrectionComment);
        if (null != member.getSavingsOnSetupCorrectionComment()) {
            if (!member.getSavingsOnSetupCorrectionComment().isEmpty()) {
                txtAMMiddleCycleSavingsComment.setText(member.getSavingsOnSetupCorrectionComment());
            }
        }

        EditText txtAMMiddleCycleLoansComment = (EditText) findViewById(R.id.txtAMMiddleCycleLoansCorrectionComment);
        if (null != member.getOutstandingLoanOnSetupCorrectionComment()) {
            if (!member.getOutstandingLoanOnSetupCorrectionComment().isEmpty()) {
                txtAMMiddleCycleLoansComment.setText(member.getOutstandingLoanOnSetupCorrectionComment());
            }
        }

        //Show the heading
        //Get the date of the dummy GSW meeting
        MeetingRepo meetingRepo = new MeetingRepo(getBaseContext());

        String pronoun = member.getGender().startsWith("F") || member.getGender().startsWith("f") ? "her" : "his";
        lblAMMiddleCycleInformationHeading.setText("This member’s information was added after the cycle started. Here are " + pronoun + " total savings and outstanding loans on that day.");

        Meeting dummyGSWMeeting = meetingRepo.getDummyGettingStartedWizardMeeting();
        if (dummyGSWMeeting != null) {
            lblAMMiddleCycleInformationHeading.setText("This member’s information was added on " + Utils.formatDate(dummyGSWMeeting.getMeetingDate(), "dd MMM yyyy") + " after the cycle started. Here are " + pronoun + " total savings and outstanding loans on that day.");
        }

        /** if meeting is after 2nd meeting, disable editing outstanding balance*/
        if(meetingRepo.getAllNonGSWMeetings().size()>2){
            TextView lblAMMiddleCycleSavingsCorrectionLabel = (TextView) findViewById(R.id.lblAMMiddleCycleSavingsCorrectionLabel);
            lblAMMiddleCycleSavingsCorrectionLabel.setVisibility(View.GONE);
            EditText txtAMMiddleCycleSavingsCorrection = (EditText) findViewById(R.id.txtAMMiddleCycleSavingsCorrection);
            LinearLayout linearLayout = (LinearLayout)txtAMMiddleCycleLoansComment.getParent();
            linearLayout.setVisibility(View.GONE);

            TextView lblAMMiddleCycleLoansCorrectionLabel = (TextView) findViewById(R.id.lblAMMiddleCycleLoansCorrectionLabel);
            lblAMMiddleCycleLoansCorrectionLabel.setVisibility(View.GONE);
            EditText txtAMMiddleCycleLoansCorrection = (EditText) findViewById(R.id.txtAMMiddleCycleLoansCorrection);
            LinearLayout linearLayout2 = (LinearLayout)txtAMMiddleCycleLoansComment.getParent();
            linearLayout.setVisibility(View.GONE);

        }

    }

    private void inflateCustomActionBar() {

        // BEGIN_INCLUDE (inflate_set_custom_view)
        // Inflate a "Done/Cancel" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) getSupportActionBar().getThemedContext()
                .getSystemService(LAYOUT_INFLATER_SERVICE);
        View customActionBarView = null;
        ActionBar actionBar = getSupportActionBar();

        // Swap in training mode icon if in training mode
        if (Utils.isExecutingInTrainingMode()) {
            actionBar.setIcon(R.drawable.icon_training_mode);
        }

        if (isEditAction) {
            customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_cancel_done, null);
            customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedFinishButton = true;
                            if (saveMemberData()) {
                                finish();
                            }
                        }
                    }
            );
            customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            finish();
                        }
                    }
            );

            actionBar.setTitle("Edit Member");
        } else {
            customActionBarView = inflater.inflate(R.layout.actionbar_custom_view_done_next_cancel, null);
            customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedFinishButton = true;
                            saveMemberData();
                            finish();
                        }
                    }
            );
            customActionBarView.findViewById(R.id.actionbar_enter_next).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            saveMemberData();
                        }
                    }
            );
            customActionBarView.findViewById(R.id.actionbar_cancel).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            finish();
                        }
                    }
            );


            actionBar.setTitle("New Member");
        }

        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        actionBar.setCustomView(customActionBarView,
                new ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL)
        );

        actionBar.setDisplayShowCustomEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        final MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.add_member, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent upIntent = new Intent(this, MainActivity.class);
                if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
                    TaskStackBuilder
                            .from(this)
                            .addNextIntent(new Intent(this, MainActivity.class))
                            .addNextIntent(upIntent).startActivities();
                    finish();
                } else {
                    NavUtils.navigateUpTo(this, upIntent);
                }
                return true;
            case R.id.mnuAMNext:
                //Toast.makeText(getBaseContext(), "You have successfully added a new member", Toast.LENGTH_LONG).show();
                return saveMemberData();
            case R.id.mnuAMFinished:
                selectedFinishButton = true;
                return saveMemberData();
        }
        return true;
    }

    boolean saveMemberData() {
        boolean successFlg = false;
        AlertDialog dlg = null;

        Member member = new Member();
        repo = new MemberRepo(getApplicationContext());
        if (selectedMember != null) {
            member = selectedMember;
        }

        if (validateData(member)) {
            boolean retVal = false;
            if (member.getMemberId() != 0) {
                retVal = repo.updateMember(member);
            } else {
                retVal = repo.addMember(member);

            }
            if (retVal) {
                if (member.getMemberId() == 0) {
                    //Set this new entity as the selected one
                    //Due to this ensure empty fields are explicitly set to null or default value
                    //Otherwise they will assume the value of the selectedMember variable because it is not null
                    selectedMember = member;

                    if (selectedFinishButton) {
                        Toast toast = Toast.makeText(this, "The new member was added successfully.", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.LEFT, 0, 0);
                        toast.show();
                        if (Utils._membersAccessedFromNewCycle) {
                            Intent i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
                            i.putExtra("_isUpdateCycleAction", false);
                            startActivity(i);
                        } else if (Utils._membersAccessedFromEditCycle) {
                            Intent i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
                            i.putExtra("_isUpdateCycleAction", true);
                            startActivity(i);
                        } else {
                            Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                            startActivity(i);
                        }
                        Utils._membersAccessedFromNewCycle = false;
                        Utils._membersAccessedFromEditCycle = false;
                    } else {
                        Toast toast = Toast.makeText(this, "The new member was added successfully. Add another member.", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.LEFT, 0, 0);
                        toast.show();
                        //Clear the Fields and keep adding new records
                        clearDataFields();
                    }

                    selectedFinishButton = false;

                    /*
                    dlg = Utils.createAlertDialog(AddMemberActivity.this,"Add Member","The new member was added successfully.", Utils.MSGBOX_ICON_TICK);
                    // Setting OK Button
                    dlg.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(successAlertDialogShown) {
                                if(selectedFinishButton) {
                                    if(Utils._membersAccessedFromNewCycle) {
                                        Intent i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
                                        i.putExtra("_isUpdateCycleAction", false);
                                        startActivity(i);
                                    }
                                    else if(Utils._membersAccessedFromEditCycle) {
                                        Intent i = new Intent(getApplicationContext(), NewCyclePg2Activity.class);
                                        i.putExtra("_isUpdateCycleAction", true);
                                        startActivity(i);
                                    }
                                    else {
                                        Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                                        startActivity(i);
                                    }
                                    Utils._membersAccessedFromNewCycle = false;
                                    Utils._membersAccessedFromEditCycle = false;
                                }
                                else {
                                    //Clear the Fields and keep adding new records
                                    clearDataFields();
                                }

                                selectedFinishButton = false;
                                successAlertDialogShown = false;
                            }
                        }
                    });
                    dlg.show();
                    */
                } else {
                    Toast.makeText(this, "The member was updated successfully.", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                    startActivity(i);

                    /*
                    dlg = Utils.createAlertDialog(AddMemberActivity.this,"Edit Member","The member was updated successfully.", Utils.MSGBOX_ICON_TICK);
                    // Setting OK Button
                    dlg.setButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(successAlertDialogShown) {
                                Intent i = new Intent(getApplicationContext(), MembersListActivity.class);
                                startActivity(i);
                                successAlertDialogShown = false;
                            }
                        }
                    });
                    dlg.show();
                    */
                }

                /*
                if(dlg.isShowing()) {
                    //Flag that ready to goto Next
                    successAlertDialogShown = true;
                }
                */

                successFlg = true;
                //clearDataFields(); //Not needed now
            } else {
                dlg = Utils.createAlertDialogOk(AddMemberActivity.this, "Add Member", "A problem occurred while adding the new member.", Utils.MSGBOX_ICON_TICK);
                dlg.show();
            }
        } else {
            //displayMessageBox(dialogTitle, "Validation Failed! Please check your entries and try again.", MSGBOX_ICON_EXCLAMATION);
        }

        return successFlg;
    }

    boolean validateData(Member member) {
        try {
            if (null == member) {
                return false;
            }
            repo = new MemberRepo(getApplicationContext());

            // Validate: MemberNo
            Spinner cboAMMemberNo = (Spinner) findViewById(R.id.cboAMMemberNo);
            String dlgTitle = "Add Member";
            if (cboAMMemberNo.getSelectedItemPosition() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The member number is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboAMMemberNo.requestFocus();
                return false;
            } else {
                String memberNo = cboAMMemberNo.getSelectedItem().toString().trim();
                int theMemberNo = Integer.parseInt(memberNo);
                member.setMemberNo(theMemberNo);
            }

            //Validate: Surname
            TextView txtSurname = (TextView) findViewById(R.id.txtAMSurname);
            String surname = txtSurname.getText().toString().trim();
            if (surname.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The Surname is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtSurname.requestFocus();
                return false;
            } else {
                member.setSurname(surname);
            }

            //Validate: OtherNames
            TextView txtOtherNames = (TextView) findViewById(R.id.txtAMOtherName);
            String otherNames = txtOtherNames.getText().toString().trim();
            if (otherNames.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "At least one other name is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtOtherNames.requestFocus();
                return false;
            } else {
                member.setOtherNames(otherNames);
            }

            //Validate: Gender
            //TextView txtGender = (TextView)findViewById(R.id.txtAMGender);
            Spinner cboGender = (Spinner) findViewById(R.id.cboAMGender);
            if (cboGender.getSelectedItemPosition() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The sex is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboGender.requestFocus();
                return false;
            } else {
                String gender = cboGender.getSelectedItem().toString().trim();
                member.setGender(gender);
            }

            // Validate: Age
            Spinner cboAge = (Spinner) findViewById(R.id.cboAMAge);
            if (cboAge.getSelectedItemPosition() == 0) {
                Utils.createAlertDialogOk(this, dlgTitle, "The age is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboAge.requestFocus();
                return false;
            } else {
                String age = cboAge.getSelectedItem().toString().trim();
                Integer theAge = Integer.parseInt(age);
                Calendar c = Calendar.getInstance();
                c.add(Calendar.YEAR, -theAge);
                member.setDateOfBirth(c.getTime());
            }

            //Validate: Occupation
            TextView txtOccupation = (TextView) findViewById(R.id.txtAMOccupation);
            String occupation = txtOccupation.getText().toString().trim();
            if (occupation.length() < 1) {
                Utils.createAlertDialogOk(this, dlgTitle, "The Occupation is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtOccupation.requestFocus();
                return false;
            } else {
                member.setOccupation(occupation);
            }

            //Validate: PhoneNumber
            TextView txtPhoneNo = (TextView) findViewById(R.id.txtAMPhoneNo);
            String phoneNo = txtPhoneNo.getText().toString().trim();
            if (phoneNo.length() < 1) {
                //Utils.createAlertDialogOk(AddMemberActivity.this, dlgTitle, "The Phone Number is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                //txtPhoneNo.requestFocus();
                //return false;
                member.setPhoneNumber(null);
            } else {
                member.setPhoneNumber(phoneNo.replaceAll(" ", "")); //remove smart formattings
            }

            // Validate: Cycles Completed
            Spinner cboAMCycles = (Spinner) findViewById(R.id.cboAMCycles);
            if (cboAMCycles.getSelectedItemPosition() == 0) {
                Utils.createAlertDialogOk(this, dlgTitle, "The cycles completed field is required.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboAMCycles.requestFocus();
                return false;
            } else {
                String cycles = cboAMCycles.getSelectedItem().toString().trim();
                Integer theCycles = Integer.parseInt(cycles);
                if (theCycles > 100) {
                    Utils.createAlertDialogOk(this, dlgTitle, "The number of completed cycles is too high.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                    cboAMCycles.requestFocus();
                    return false;
                }
                member.setCyclesCompleted(theCycles);
                Calendar c = Calendar.getInstance();
                c.add(Calendar.YEAR, -theCycles);
                member.setDateOfAdmission(c.getTime());
            }

            TextView txtSavingsSoFar = (TextView) findViewById(R.id.txtAMMiddleCycleSavingsCorrection);
            String savings = txtSavingsSoFar.getText().toString().trim();
            if (savings.length() < 1) {
                if (isEditAction) {
                    member.setSavingsOnSetup(member.getSavingsOnSetup());
                } else {
                    member.setSavingsOnSetup(0);
                }
            } else {
                double amountSavedSoFar = Double.parseDouble(savings);
                if (amountSavedSoFar < 0.00) {
                    Utils.createAlertDialogOk(this, dlgTitle, "Total Amount this Member has Saved in Current Cycle so far should be zero and above.", Utils.MSGBOX_ICON_EXCLAMATION);
                    txtSavingsSoFar.requestFocus();
                    return false;
                } else {
                    member.setSavingsOnSetup(amountSavedSoFar);
                }
            }

            //Validate Amount of Loan outstanding for this member
            TextView txtLoanAmount = (TextView) findViewById(R.id.txtAMMiddleCycleLoansCorrection);
            String loanAmount = txtLoanAmount.getText().toString().trim();
            if (loanAmount.length() < 1) {
                if (isEditAction) {
                    member.setOutstandingLoanOnSetup(member.getOutstandingLoanOnSetup());
                } else {
                    member.setOutstandingLoanOnSetup(0);
                }
            } else {
                double outstandingLoan = Double.parseDouble(loanAmount);
                if (outstandingLoan < 0.00) {
                    Utils.createAlertDialogOk(this, dlgTitle, "Total Amount of this Member's Regular Loan Outstanding should be zero and above.", Utils.MSGBOX_ICON_EXCLAMATION);
                    txtLoanAmount.requestFocus();
                    return false;
                } else {
                    member.setOutstandingLoanOnSetup(outstandingLoan);

                    //set the date of next repayment
                    if (outstandingLoan > 0 && txtAMMLoanNextRepaymentDate.getText().length() == 0) {
                        Utils.createAlertDialogOk(this, dlgTitle, "The next repayment date is required for the outstanding loan", Utils.MSGBOX_ICON_EXCLAMATION);
                        txtAMMLoanNextRepaymentDate.requestFocus();
                        return false;
                    }

                    //set the loan number
                    if (outstandingLoan > 0 && txtAMMLoanNumber.getText().length() == 0) {
                        Utils.createAlertDialogOk(this, dlgTitle, "The loan number is required for the outstanding loan", Utils.MSGBOX_ICON_EXCLAMATION);
                        txtAMMLoanNumber.requestFocus();
                        return false;
                    }

                }

            }
            member.setOutstandingLoanNumberOnSetup(Integer.valueOf(txtAMMLoanNumber.getText().toString().trim()));
            member.setDateOfFirstRepayment(Utils.getDateFromString(txtAMMLoanNextRepaymentDate.getText().toString(), "dd-MMM-yyyy"));

            validateMiddleCycleValues(member);
            //Final Verifications
            //TODO: Trying to use Application context to ensure dialog box does not disappear
            if (!repo.isMemberNoAvailable(member.getMemberNo(), member.getMemberId())) {
                Utils.createAlertDialogOk(this, dlgTitle, "Another member is using this Member Number.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                cboAMMemberNo.requestFocus();
                return false;
            }

            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    // TODO: Resolve Redundancy
    private void validateMiddleCycleValues(Member member) {
        //If edit mode and not GSW, validate middle cycle start values
        if (!isGettingStartedMode) {
            //if there are corrections, set them

            TextView txtAMMiddleCycleSavingsCorrection = (TextView) findViewById(R.id.txtAMMiddleCycleSavingsCorrection);
            TextView txtAMMiddleCycleLoansCorrection = (TextView) findViewById(R.id.txtAMMiddleCycleLoansCorrection);
            TextView txtAMMiddleCycleSavingsCorrectionComment = (TextView) findViewById(R.id.txtAMMiddleCycleSavingsCorrectionComment);
            TextView txtAMMiddleCycleLoansCorrectionComment = (TextView) findViewById(R.id.txtAMMiddleCycleLoansCorrectionComment);

            if (txtAMMiddleCycleSavingsCorrection.getText().length() > 0) {
                member.setSavingsOnSetup(Double.parseDouble(txtAMMiddleCycleSavingsCorrection.getText().toString()));
            }

            if (!txtAMMiddleCycleSavingsCorrectionComment.getText().toString().isEmpty()) {
                member.setSavingsOnSetupCorrectionComment(txtAMMiddleCycleSavingsCorrectionComment.getText().toString());
            }

            if (txtAMMiddleCycleLoansCorrection.getText().length() > 0) {
                member.setOutstandingLoanOnSetup(Double.parseDouble(txtAMMiddleCycleLoansCorrection.getText().toString()));
            }

            if (!txtAMMiddleCycleLoansCorrectionComment.getText().toString().isEmpty()) {
                Log.d("AMA", txtAMMiddleCycleLoansCorrectionComment.getText().toString());
                member.setOutstandingLoanOnSetupCorrectionComment(txtAMMiddleCycleLoansCorrectionComment.getText().toString());
            }

        }
    }

    void populateDataFields(final Member member) {
        try {

            clearDataFields();
            if (member == null) {
                return;
            }

            TextView txtSurname = (TextView) findViewById(R.id.txtAMSurname);
            if (member.getSurname() != null) {
                txtSurname.setText(member.getSurname());
            }
            TextView txtOtherNames = (TextView) findViewById(R.id.txtAMOtherName);
            if (member.getOtherNames() != null) {
                txtOtherNames.setText(member.getOtherNames());
            }

            Spinner cboGender = (Spinner) findViewById(R.id.cboAMGender);
            if (member.getGender() != null) {
                if (member.getGender().startsWith("F") || member.getGender().startsWith("f")) {
                    cboGender.setSelection(2);
                } else if (member.getGender().startsWith("M") || member.getGender().startsWith("m")) {
                    cboGender.setSelection(1);
                }
            }

            TextView txtOccupation = (TextView) findViewById(R.id.txtAMOccupation);
            if (member.getOccupation() != null) {
                txtOccupation.setText(member.getOccupation());
            }
            TextView txtPhone = (TextView) findViewById(R.id.txtAMPhoneNo);
            if (member.getPhoneNumber() != null) {
                txtPhone.setText(Utils.formatAsPhoneNumber(member.getPhoneNumber()));
            }

            // Set the age
            final Spinner cboAMAge = (Spinner) findViewById(R.id.cboAMAge);
            Calendar calToday = Calendar.getInstance();
            Calendar calDb = Calendar.getInstance();
            calDb.setTime(member.getDateOfBirth());
            final int computedAge = calToday.get(Calendar.YEAR) - calDb.get(Calendar.YEAR);

            cboAMAge.post(new Runnable() {
                @Override
                public void run() {

                    Utils.setSpinnerSelection(computedAge + "", cboAMAge);
                }
            });

            calToday = Calendar.getInstance();

            // TODO: It may be preferable to allow this field to be editable if members are allowed to take leave
            // Set cycles
            final Spinner cboAMCycles = (Spinner) findViewById(R.id.cboAMCycles);
            Calendar calDbCycles = Calendar.getInstance();
            calDbCycles.setTime(member.getDateOfAdmission());
            final int cycles = calToday.get(Calendar.YEAR) - calDbCycles.get(Calendar.YEAR);
            cboAMCycles.post(new Runnable() {
                @Override
                public void run() {

                    Utils.setSpinnerSelection(cycles + "", cboAMCycles);
                }
            });

            //Load GSW values
            if (!isGettingStartedMode) {
                showMiddleStartCycleValues(member);
            }
        } finally {

        }


    }


    void clearDataFields() {
        //Spinner items
        buildGenderSpinner();
        //This portion could take long so run it as long task
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                buildMemberNoSpinner();
            }
        };
        LongTaskRunner.runLongTask(runnable, "Please wait...", "Please wait a moment...", AddMemberActivity.this);
        //buildMemberNoSpinner();

        buildAgeSpinner();
        buildCyclesCompletedSpinner();

        // Populate the Fields
        Spinner cboAMMemberNo = (Spinner) findViewById(R.id.cboAMMemberNo);

        TextView txtSurname = (TextView) findViewById(R.id.txtAMSurname);
        txtSurname.setText(null);
        TextView txtOtherNames = (TextView) findViewById(R.id.txtAMOtherName);
        txtOtherNames.setText(null);
        //TextView txtGender = (TextView)findViewById(R.id.txtAMGender);
        //txtGender.setText(null);
        TextView txtOccupation = (TextView) findViewById(R.id.txtAMOccupation);
        txtOccupation.setText(null);
        TextView txtPhone = (TextView) findViewById(R.id.txtAMPhoneNo);
        txtPhone.setText(null);

        /**TextView txtLoanNumber = (TextView) findViewById(R.id.txtAMMOutstandingLoanNumber);
         txtLoanNumber.setText(null); */

        cboAMMemberNo.requestFocus();
    }

    void updateDisplay() {
        if (viewClicked != null) {
            viewClicked.setText(new StringBuilder()

                    // Month is 0 based so add 1
                    .append(String.format("%02d", mDay))
                    .append("-")
                    .append(Utils.getMonthNameAbbrev(mMonth + 1))
                    .append("-")
                    .append(mYear)
                    .toString());
        }
    }


    private void buildGenderSpinner() {

        //Setup the Spinner Items
        Spinner cboGender = (Spinner) findViewById(R.id.cboAMGender);
        String[] genderList = new String[]{"select sex", "Male", "Female"};
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, genderList) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
                ((TextView) v).setTypeface(externalFont);

                // ((TextView) v).setTextAppearance(getApplicationContext(), R.style.RegularText);

                return v;
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
                ((TextView) v).setTypeface(externalFont);

                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboGender.setAdapter(adapter);
        cboGender.setOnItemSelectedListener(new CustomGenderSpinnerListener());

        //Make the spinner selectable
        cboGender.setFocusable(true);
        cboGender.setFocusableInTouchMode(true);
        cboGender.setClickable(true);
    }

    /* Populates the member no spinner with available member numbers */
    void buildMemberNoSpinner() {


        repo = new MemberRepo(getApplicationContext());
        final ArrayList<String> memberNumberArrayList = new ArrayList<String>();
        memberNumberArrayList.add("select number");
        //If we have a selected member, then add the member number to the adapter
        if (selectedMember != null && selectedMember.getMemberNo() != 0) {
            memberNumberArrayList.add(selectedMember.getMemberNo() + "");
        }


        for (String mNo : repo.getListOfAvailableMemberNumbers()) {
            Log.d(getBaseContext().getPackageName(), "Member number found " + mNo);
            memberNumberArrayList.add(mNo);
        }


        String[] memberNumberList = memberNumberArrayList.toArray(new String[memberNumberArrayList.size()]);
        memberNumberArrayList.toArray(memberNumberList);
        final ArrayAdapter<CharSequence> memberNoAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, memberNumberList) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
                ((TextView) v).setTypeface(externalFont);

                // ((TextView) v).setTextAppearance(getApplicationContext(), R.style.RegularText);

                return v;
            }


            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
                ((TextView) v).setTypeface(externalFont);

                return v;
            }
        };

        memberNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                cboAMMemberNo.setAdapter(memberNoAdapter);
                // Populate the Fields
                if (selectedMember != null) {
                    cboAMMemberNo.post(new Runnable() {
                        @Override
                        public void run() {

                            Utils.setSpinnerSelection(selectedMember.getMemberNo() + "", cboAMMemberNo);
                        }
                    });
                }
            }
        };
        runOnUiThread(runnable);

        cboAMMemberNo.setOnItemSelectedListener(new CustomGenderSpinnerListener());
        //Make the spinner selectable
        cboAMMemberNo.setFocusable(true);
        cboAMMemberNo.setFocusableInTouchMode(true);
        cboAMMemberNo.setClickable(true);
    }

    /* Populates the member age spinner  */
    protected void buildAgeSpinner() {

        Spinner cboAMAge = (Spinner) findViewById(R.id.cboAMAge);
        repo = new MemberRepo(getApplicationContext());
        ArrayList<String> ageArrayList = new ArrayList<String>();
        ageArrayList.add("select age");
        for (int i = 12; i <= 80; i++) {
            ageArrayList.add(i + "");
        }
        String[] ageList = ageArrayList.toArray(new String[ageArrayList.size()]);
        ageArrayList.toArray(ageList);
        ArrayAdapter<CharSequence> memberNoAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, ageList) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
                ((TextView) v).setTypeface(externalFont);

                // ((TextView) v).setTextAppearance(getApplicationContext(), R.style.RegularText);

                return v;
            }

            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
                ((TextView) v).setTypeface(externalFont);

                return v;
            }
        };
        memberNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboAMAge.setAdapter(memberNoAdapter);
        cboAMAge.setOnItemSelectedListener(new CustomGenderSpinnerListener());
        //Make the spinner selectable
        cboAMAge.setFocusable(true);
        cboAMAge.setFocusableInTouchMode(true);
        cboAMAge.setClickable(true);
    }

    /* Populates the member cycles completed spinner */
    protected void buildCyclesCompletedSpinner() {

        Spinner cboAMCycles = (Spinner) findViewById(R.id.cboAMCycles);
        repo = new MemberRepo(getApplicationContext());
        ArrayList<String> cyclesArrayList = new ArrayList<String>();
        cyclesArrayList.add("select number");
        for (int i = 0; i <= 20; i++) {
            cyclesArrayList.add(i + "");
        }
        String[] ageList = cyclesArrayList.toArray(new String[cyclesArrayList.size()]);
        cyclesArrayList.toArray(ageList);
        ArrayAdapter<CharSequence> memberNoAdapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, ageList) {
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
                ((TextView) v).setTypeface(externalFont);

                // ((TextView) v).setTextAppearance(getApplicationContext(), R.style.RegularText);

                return v;
            }


            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);

                Typeface externalFont = Typeface.createFromAsset(getAssets(), "fonts/roboto-regular.ttf");
                ((TextView) v).setTypeface(externalFont);

                return v;
            }
        };


        memberNoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cboAMCycles.setAdapter(memberNoAdapter);
        cboAMCycles.setOnItemSelectedListener(new CustomGenderSpinnerListener());

        // Make the spinner selectable
        cboAMCycles.setFocusable(true);
        cboAMCycles.setFocusableInTouchMode(true);
        cboAMCycles.setClickable(true);
    }


}