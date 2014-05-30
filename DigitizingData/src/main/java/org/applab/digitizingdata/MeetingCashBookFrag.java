package org.applab.digitizingdata;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.MenuItem;

import org.applab.digitizingdata.domain.model.MeetingFine;
import org.applab.digitizingdata.domain.model.MeetingStartingCash;
import org.applab.digitizingdata.helpers.Utils;
import org.applab.digitizingdata.repo.MeetingFineRepo;
import org.applab.digitizingdata.repo.MeetingLoanIssuedRepo;
import org.applab.digitizingdata.repo.MeetingLoanRepaymentRepo;
import org.applab.digitizingdata.repo.MeetingRepo;
import org.applab.digitizingdata.repo.MeetingSavingRepo;

public class MeetingCashBookFrag extends SherlockFragment {

    ActionBar actionBar = null;
    String meetingDate = null;
    int meetingId = 0;
    double cashToBank = 0.0;
    double cashToBox = 0.0;
    MeetingRepo meetingRepo = null;
    MeetingSavingRepo savingRepo = null;
    MeetingLoanRepaymentRepo repaymentRepo = null;
    MeetingLoanIssuedRepo loanIssuedRepo = null;
    MeetingFineRepo fineRepo = null;
    MeetingStartingCash startingCashDetails = null;

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
        String title = String.format("Meeting    %s", meetingDate);
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
        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);
    /**    TextView lblMeetingDate = (TextView) getSherlockActivity().findViewById(R.id.lblMCBFMeetingDate);
        meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
        lblMeetingDate.setText(meetingDate); */

        populateCashBookFields();
    }

    @Override
    public void onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu, com.actionbarsherlock.view.MenuInflater inflater) {
        menu.clear();
        getSherlockActivity().getSupportMenuInflater().inflate(R.menu.meeting_cash_book, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                return false;
          /**  case R.id.mnuSMDSend:
                return false;
            case R.id.mnuSMDCancel:
                return false; */
            case R.id.mnuMCBFSave:
                Toast.makeText(getSherlockActivity().getApplicationContext(), "The Cashbook balances have been saved successfully.", Toast.LENGTH_LONG).show();
                return true;
            default:
                return false;
        }
    }

    private void populateCashBookFields() {


        try {
            /**    meetingRepo = new MeetingRepo(getSherlockActivity().getApplicationContext());
             savingRepo = new MeetingSavingRepo(getSherlockActivity().getApplicationContext());
             repaymentRepo = new MeetingLoanRepaymentRepo(getSherlockActivity().getApplicationContext());
             loanIssuedRepo = new MeetingLoanIssuedRepo(getSherlockActivity().getApplicationContext());
             */
            TextView lblTotalCashInBox = (TextView) getSherlockActivity().findViewById(R.id.lblExpectedStartingCash);
            TextView lblExpectedStartingCash = (TextView) getSherlockActivity().findViewById(R.id.lblExpectedStartingCash);
            TextView lblActualStartingCash = (TextView) getSherlockActivity().findViewById(R.id.lblActualStartingCash);
            TextView lblCashDifference = (TextView) getSherlockActivity().findViewById(R.id.lblCashDifference);
            TextView lblCashBookComment = (TextView) getSherlockActivity().findViewById(R.id.lblCashBookComment);

            TextView lblSavings = (TextView) getSherlockActivity().findViewById(R.id.lblSavings);
            TextView lblLoanRepayments = (TextView) getSherlockActivity().findViewById(R.id.lblLoanRepayments);
            TextView lblFines = (TextView) getSherlockActivity().findViewById(R.id.lblFines);
            TextView lblNewLoans = (TextView) getSherlockActivity().findViewById(R.id.lblNewLoans);



            /**       TextView lblTotalCash = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFTotalCashIn);
             TextView lblOpeningCash = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFOpeningCash);
             TextView lblTotalSavings = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFSavings);
             TextView lblTotalLoansRepaid = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFLoansRepaid);
             TextView lblTotalLoansIssued = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFLoansIssued);
             TextView lblTotalCashOut = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFTotalCashOut);
             TextView lblTotalCashBalance = (TextView)getSherlockActivity().findViewById(R.id.lblMCBFBalBd);
             */
            startingCashDetails = meetingRepo.getMeetingActualStartingCashDetails(meetingId);
            double expectedStartingCash = 0.0;

            // = startingCashDetails.getExpectedStartingCash();


            double totalSavings = savingRepo.getTotalSavingsInMeeting(meetingId);
            double totalLoansRepaid = repaymentRepo.getTotalLoansRepaidInMeeting(meetingId);
            double totalLoansIssued = loanIssuedRepo.getTotalLoansIssuedInMeeting(meetingId);
            double totalFines = fineRepo.getTotalFinesInMeeting(meetingId);

            double actualStartingCash = startingCashDetails.getActualStartingCash();


            double totalCashOut = totalLoansIssued;
            double totalCashIn = actualStartingCash + totalSavings + totalLoansRepaid + totalFines;

            expectedStartingCash = totalSavings + totalLoansRepaid - totalLoansIssued - cashToBank + totalFines;

            cashToBox = totalCashIn - totalCashOut;
            String comment = startingCashDetails.getComment();

            Log.d("Select Start CashFrag", startingCashDetails.getComment());


            lblTotalCashInBox.setText(String.format("Total Cash In Box %,.0f UGX", cashToBox));
            lblExpectedStartingCash.setText(String.format("Expected Starting Cash %,.0f UGX", expectedStartingCash));
            lblActualStartingCash.setText(String.format("Actual Starting Cash %,.0f UGX", actualStartingCash));
            lblCashDifference.setText(String.format("Difference %,.0f UGX", expectedStartingCash - actualStartingCash));
            lblCashBookComment.setText(String.format("Comment %s", comment));

            lblSavings.setText(String.format("Savings %,.0f UGX", totalSavings));
            lblLoanRepayments.setText(String.format("Loan Repayment %,.0f UGX", totalLoansRepaid));
            lblFines.setText(String.format("Fines %,.0f UGX", totalFines));
            lblNewLoans.setText(String.format("New Loans %,.0f UGX", totalLoansIssued));


            /**       lblTotalCash.setText(String.format("Total Collected: %,.0fUGX", totalCashIn));
             lblOpeningCash.setText(String.format("Starting Cash: %,.0fUGX", startingCash));
             lblTotalSavings.setText(String.format("Savings: %,.0fUGX", totalSavings));
             lblTotalLoansRepaid.setText(String.format("Loans Repaid: %,.0fUGX", totalLoansRepaid));
             lblTotalLoansIssued.setText(String.format("Loans Issued: %,.0fUGX", totalLoansIssued));
             lblTotalCashOut.setText(String.format("Total Issued: %,.0fUGX", totalCashOut));
             lblTotalCashBalance.setText(String.format("Cash Balance: %,.0fUGX", cashBalance));
             */
        } catch (Exception ex) {

        } finally {
            meetingRepo = null;
            savingRepo = null;
            repaymentRepo = null;
        }
    }

    private void updateCashBook() {
        TextView lblCashToBankAmount = (TextView) getSherlockActivity().findViewById(R.id.lblCashToBankAmount);
        cashToBank = Double.valueOf(lblCashToBankAmount.getText().toString());

        double cashSavedInBank = startingCashDetails.getCashSavedInBank();
        cashToBank = cashToBank + cashSavedInBank;
        meetingRepo.updateCashBook(meetingId, cashToBox, cashToBank);
    }
}
