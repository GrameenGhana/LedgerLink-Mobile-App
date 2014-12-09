package org.applab.digitizingdata;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingStartingCash;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

public class MeetingCashBookFrag extends SherlockFragment {

    private ActionBar actionBar = null;
    private String meetingDate = null;
    private int meetingId = 0;
    private double cashToBank = 0.0;
    double cashToBox = 0.0;
    private double totalCashInBox = 0.0;
    private MeetingRepo meetingRepo = null;
    private MeetingSavingRepo savingRepo = null;
    private MeetingLoanRepaymentRepo repaymentRepo = null;
    private MeetingLoanIssuedRepo loanIssuedRepo = null;
    private MeetingFineRepo fineRepo = null;
    private MeetingStartingCash startingCashDetails = null;
    MeetingStartingCash currentStartingCashDetails = null;
    private MeetingStartingCash previousClosingCash = null;
    private MeetingActivity parentActivity; //to access parent meeting activity
    private EditText txtCashToBankAmount;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            return null;
        }
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.frag_meeting_cash_book, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        actionBar = getSherlockActivity().getSupportActionBar();
        meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
        String title = "Meeting";
        switch (Utils._meetingDataViewMode) {
            case VIEW_MODE_REVIEW:
                title = "Send Data";
                break;
            case VIEW_MODE_READ_ONLY:
                title = "Sent Data";
                break;
            default:
                //  title = "Meeting";
                break;
        }

        meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());
        savingRepo = new MeetingSavingRepo(getSherlockActivity().getApplicationContext());
        loanIssuedRepo = new MeetingLoanIssuedRepo(getSherlockActivity().getApplicationContext());
        repaymentRepo = new MeetingLoanRepaymentRepo(getSherlockActivity().getApplicationContext());
        fineRepo = new MeetingFineRepo(getSherlockActivity().getApplicationContext());


        actionBar.setTitle(title);
        actionBar.setSubtitle(meetingDate);
        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);

        /**    TextView lblMeetingDate = (TextView) getSherlockActivity().findViewById(R.id.lblMCBFMeetingDate);
         meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
         lblMeetingDate.setText(meetingDate); */

        parentActivity = (MeetingActivity) getSherlockActivity();
        populateCashBookFields();

    }

    @Override
    public void onPause() {
        super.onPause();
        //Save only if not in view only
        if (parentActivity.isViewOnly()) {
            Toast.makeText(getSherlockActivity().getApplicationContext(), "Values for past meeting cannot be modified at this time", Toast.LENGTH_LONG).show();
        }
        if (Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
            updateCashBook();
            Toast.makeText(getSherlockActivity().getApplicationContext(), "The Cashbook balances have been saved successfully.", Toast.LENGTH_LONG).show();

        }
    }

    private void populateCashBookFields() {

        TextView lblTotalCashInBox = (TextView) getSherlockActivity().findViewById(R.id.lblTotalCashInBox);
        TextView lblExpectedStartingCash = (TextView) getSherlockActivity().findViewById(R.id.lblExpectedStartingCash);
        TextView lblActualStartingCash = (TextView) getSherlockActivity().findViewById(R.id.lblActualStartingCash);
        TextView lblCashDifference = (TextView) getSherlockActivity().findViewById(R.id.lblCashDifference);
        TextView lblCashBookComment = (TextView) getSherlockActivity().findViewById(R.id.lblCashBookComment);

        TextView lblSavings = (TextView) getSherlockActivity().findViewById(R.id.lblSavings);
        TextView lblLoanRepayments = (TextView) getSherlockActivity().findViewById(R.id.lblLoanRepayments);
        TextView lblFines = (TextView) getSherlockActivity().findViewById(R.id.lblFines);
        TextView lblNewLoans = (TextView) getSherlockActivity().findViewById(R.id.lblNewLoans);
        EditText txtCashToBankAmount = (EditText) getSherlockActivity().findViewById(R.id.txtCashToBank);

        double actualStartingCash = 0.0;
        double totalSavings = 0.0;
        double totalLoansRepaid = 0.0;
        double totalLoansIssued = 0.0;
        double totalFines = 0.0;

        double totalLoanTopUps = 0.0;

        String comment = "";


        // Lock fields in read-only mode
        //Do not invoke the event when in Read only Mode
        if (parentActivity.isViewOnly()) {
            Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
            txtCashToBankAmount.setEnabled(false);
            txtCashToBankAmount.setClickable(false);
            txtCashToBankAmount.setActivated(false);

            txtCashToBankAmount.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (Utils._meetingDataViewMode == Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                        Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                    }
                }
            });
            ((LinearLayout) txtCashToBankAmount.getParent()).setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (parentActivity.isViewOnly()) {
                        Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        try {

            //Get the Cycle that contains this meeting
            Meeting currentMeeting = meetingRepo.getMeetingById(meetingId);
            Log.d("MCB expstc", "here");
            // Get the Cycle that contains previous meeting in order to get the expected starting Cash
            Meeting previousMeeting = null;
            if (null != meetingRepo) {
                previousMeeting = meetingRepo.getPreviousMeeting(currentMeeting.getVslaCycle().getCycleId(), meetingId);
            }
            Log.d("MCB expstc", "here4");
            // Get expectedStartingCash
            double expectedStartingCash = 0.0;
            /** if (previousMeeting != null) {
             startingCashDetails = meetingRepo.getMeetingStartingCash(previousMeeting.getMeetingId());
             Log.d("MCB expstc", "here5");
             expectedStartingCash = startingCashDetails.getExpectedStartingCash();
             Log.d("MCB expstc", String.valueOf(expectedStartingCash));

             totalSavings = savingRepo.getTotalSavingsInMeeting(meetingId);
             totalLoansRepaid = repaymentRepo.getTotalLoansRepaidInMeeting(meetingId);
             totalLoansIssued = loanIssuedRepo.getTotalLoansIssuedInMeeting(meetingId);
             totalFines = fineRepo.getTotalFinesPaidInThisMeeting(meetingId);
             Log.d("MCB32 acts", "here8");
             totalLoanTopUps = startingCashDetails.getLoanTopUps();
             Log.d("MCB32 acts", "here9");
             double totalCashOut = totalLoansIssued;
             double totalCashIn = actualStartingCash + totalSavings + totalLoansRepaid + totalFines;
             totalCashInBox = actualStartingCash + totalSavings + totalLoansRepaid - totalLoansIssued + totalFines + totalLoanTopUps - cashToBank;
             Log.d("MCB acts1", String.valueOf(totalCashInBox));

             if (currentStartingCashDetails.getComment() != null) {
             Log.d("MCB acts4", "not null");

             if (!currentStartingCashDetails.getComment().isEmpty()) {
             Log.d("MCB acts7", String.valueOf(totalCashInBox));

             comment = currentStartingCashDetails.getComment();
             Log.d("MCB acts4", String.valueOf(totalCashInBox));

             }
             }
             Log.d("MCB acts5", String.valueOf(totalCashInBox));

             } else {
             Log.d("MCB expstc", "here6");
             // Get today's actuals
             //startingCashDetails = meetingRepo.getMeetingActualStartingCashDetails(meetingId);
             currentStartingCashDetails = meetingRepo.getMeetingStartingCash(meetingId);
             actualStartingCash = currentStartingCashDetails.getActualStartingCash();
             Log.d("MCB33 acts", String.valueOf(actualStartingCash));
             // expectedStartingCash = startingCashDetails.getExpectedStartingCash();
             cashToBank = meetingRepo.getCashTakenToBankInPreviousMeeting(currentMeeting.getMeetingId());
             Log.d("MCB31 acts", String.valueOf(cashToBank));


             if (meetingId == meetingRepo.getDummyGettingStartedWizardMeeting().getMeetingId()) {
             totalCashInBox = currentStartingCashDetails.getExpectedStartingCash();
             expectedStartingCash = totalCashInBox;
             totalSavings = 0.0;
             totalLoansIssued = 0.0;
             totalFines = 0.0;
             totalLoansRepaid = 0.0;
             }

             Log.d("MCB acts2", String.valueOf(totalCashInBox));

             } */
            startingCashDetails = meetingRepo.getMeetingStartingCash(meetingId);
          /**  if (previousMeeting == null) {
                currentStartingCashDetails = meetingRepo.getMeetingStartingCash(meetingId);
                expectedStartingCash = currentStartingCashDetails.getExpectedStartingCash();
                // totalCashInBox = startingCashDetails.getExpectedStartingCash();
                //expectedStartingCash = totalCashInBox;
                totalSavings = 0.0;
                totalLoansIssued = 0.0;
                totalFines = 0.0;
                totalLoansRepaid = 0.0;
                Log.d("MCB acts7", String.valueOf(totalCashInBox));
            } else {
                expectedStartingCash = startingCashDetails.getExpectedStartingCash();
            } */

            // If there is a previous meeting get expected starting cash from there
            if (previousMeeting != null) {
                //targetMeetingId = previousMeeting.getMeetingId();
                if (previousMeeting.getMeetingId() != -1) {
                    previousClosingCash = meetingRepo.getMeetingStartingCash(previousMeeting.getMeetingId());

                    if (previousClosingCash != null) {
                        expectedStartingCash = previousClosingCash.getExpectedStartingCash();
                    }
                }
            } else {
                expectedStartingCash = startingCashDetails.getExpectedStartingCash();
            }
            Log.d("MCB acts now", String.valueOf(expectedStartingCash));

            totalSavings = savingRepo.getTotalSavingsInMeeting(meetingId);
            totalLoansRepaid = repaymentRepo.getTotalLoansRepaidInMeeting(meetingId);
            totalLoansIssued = loanIssuedRepo.getTotalLoansIssuedInMeeting(meetingId);
            totalFines = fineRepo.getTotalFinesPaidInThisMeeting(meetingId);

            totalLoanTopUps = startingCashDetails.getLoanTopUps();
            actualStartingCash = startingCashDetails.getActualStartingCash();
            cashToBank = meetingRepo.getCashTakenToBankInPreviousMeeting(currentMeeting.getMeetingId());

            totalCashInBox = actualStartingCash + totalSavings + totalLoansRepaid - totalLoansIssued + totalFines + totalLoanTopUps - cashToBank;

            if (meetingId == meetingRepo.getDummyGettingStartedWizardMeeting().getMeetingId()) {
                Log.d("MCB acts7", "here8");
                totalCashInBox = startingCashDetails.getExpectedStartingCash();
                expectedStartingCash = totalCashInBox;
                Log.d("MCB acts7", String.valueOf(totalCashInBox));
                totalSavings = 0.0;
                totalLoansIssued = 0.0;
                totalFines = 0.0;
                totalLoansRepaid = 0.0;

            }

            if (null != startingCashDetails.getComment()) {
                if (!startingCashDetails.getComment().isEmpty()) {
                    Log.d("MCB acts9", String.valueOf(totalCashInBox));

                    comment = startingCashDetails.getComment();
                    Log.d("MCB acts4", String.valueOf(totalCashInBox));

                }
            }
            Log.d("MCB acts11", String.valueOf(totalCashInBox));
            Log.d("MCB acts now2", String.valueOf(expectedStartingCash));
            //}
            lblTotalCashInBox.setText(String.format("Total Cash In Box %,.0f UGX", totalCashInBox));
            lblExpectedStartingCash.setText(String.format("Expected Starting Cash %,.0f UGX", expectedStartingCash));
            lblActualStartingCash.setText(String.format("Actual Starting Cash %,.0f UGX", actualStartingCash));
            lblCashDifference.setText(String.format("Difference %,.0f UGX", expectedStartingCash - actualStartingCash));
            lblCashBookComment.setText(String.format("Comment %s", comment));

            lblSavings.setText(String.format("Savings %,.0f UGX", totalSavings));
            lblLoanRepayments.setText(String.format("Loan Repayment %,.0f UGX", totalLoansRepaid));
            lblFines.setText(String.format("Fines %,.0f UGX", totalFines));
            lblNewLoans.setText(String.format("New Loans %,.0f UGX", totalLoansIssued + totalLoanTopUps));

            txtCashToBankAmount.setText(String.format("%.0f", cashToBank));
        } catch (Exception ex) {

        } finally {
            meetingRepo = null;
            savingRepo = null;
            repaymentRepo = null;
        }
    }

    private void updateCashBook() {
        if (parentActivity.isViewOnly()) {
            return;
        }
        double theCashToBank = 0.0;
        if (validate()) {
            txtCashToBankAmount = (EditText) getSherlockActivity().findViewById(R.id.txtCashToBank);
            theCashToBank = Double.valueOf(txtCashToBankAmount.getText().toString());

            Log.d("MCB2 acts", String.valueOf(totalCashInBox));
            Log.d("MCB3 acts", String.valueOf(theCashToBank));
            //cashToBox = totalCashInBox - cashToBank;

            // double cashSavedInBank = startingCashDetails.getCashSavedInBank();
            // cashToBank = cashToBank + cashSavedInBank;
            if (meetingRepo == null) {
                meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());
            }
            meetingRepo.updateCashBook(meetingId, totalCashInBox, theCashToBank);
        }
    }

    private boolean validate() {
        // Validate: Fine Amount
        txtCashToBankAmount = (EditText) getSherlockActivity().findViewById(R.id.txtCashToBank);
        String cashBook = txtCashToBankAmount.getText().toString().trim();
        if (cashBook.length() < 1) {
            return true;
        } else {
            double theCashToBank = Double.parseDouble(cashBook);
            if (theCashToBank < 0) {
                Utils.createAlertDialogOk(getSherlockActivity().getBaseContext(), "Meeting", "The value for Cash Book Box must be positive.", Utils.MSGBOX_ICON_EXCLAMATION).show();
                txtCashToBankAmount.requestFocus();
                return false;

            } else {
                return true;
            }
        }
    }
}
