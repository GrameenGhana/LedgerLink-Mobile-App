package org.applab.ledgerlink.repo;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import org.applab.ledgerlink.datatransformation.AttendanceDataTransferRecord;
import org.applab.ledgerlink.datatransformation.FinesDataTransferRecord;
import org.applab.ledgerlink.datatransformation.LoanDataTransferRecord;
import org.applab.ledgerlink.datatransformation.RepaymentDataTransferRecord;
import org.applab.ledgerlink.datatransformation.SavingsDataTransferRecord;
import org.applab.ledgerlink.domain.model.Meeting;
import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.domain.model.VslaCycle;
import org.applab.ledgerlink.domain.model.VslaInfo;
import org.applab.ledgerlink.helpers.DatabaseHandler;
import org.applab.ledgerlink.helpers.Utils;
import org.applab.ledgerlink.utils.DialogMessageBox;
import org.json.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Moses on 10/24/13.
 * Refactored By Joseph Capito 10/22/2015
 */
public class SendDataRepo {

    protected Context context;
    protected VslaCycle vslaCycle;
    protected VslaInfo vslaInfo;
    protected ArrayList<Member> members;
    protected Meeting meeting;
    protected ArrayList<AttendanceDataTransferRecord> attendances;
    protected ArrayList<SavingsDataTransferRecord> savings;
    protected ArrayList<LoanDataTransferRecord> loanIssues;
    protected ArrayList<RepaymentDataTransferRecord> loanRepayments;
    protected ArrayList<FinesDataTransferRecord> fines;
    protected boolean meetingLoaded;
    protected int meetingId;

    public SendDataRepo(Context context, int meetingId){
        this.context = context;
        this.meetingLoaded = false;
        this.meetingId = meetingId;

        this.loadVslaInfo();
        this.loadVslaCycle();
        this.loadMembers();
        this.loadMeeting();
        this.loadAttendance();
        this.loadSaving();
        this.loadLoanIssue();
        this.loadLoanRepayment();
        this.loadFines();
    }

    protected void loadVslaInfo(){
        VslaInfoRepo vslaInfoRepo = new VslaInfoRepo(context);
        this.vslaInfo = vslaInfoRepo.getVslaInfo();
    }

    protected void loadVslaCycle(){
        VslaCycleRepo vslaCycleRepo = new VslaCycleRepo(this.context);
        this.vslaCycle = vslaCycleRepo.getCurrentCycle();
    }

    protected void loadMembers(){
        MemberRepo memberRepo = new MemberRepo(context);
        members = memberRepo.getAllMembers();
    }

    protected void loadMeeting(){
        if(!this.meetingLoaded) {
            MeetingRepo meetingRepo = new MeetingRepo(context);
            meeting = meetingRepo.getMeetingById(this.meetingId);
            this.meetingLoaded = true;
        }
    }

    protected void loadAttendance(){
        if(this.meetingLoaded) {
            MeetingAttendanceRepo meetingAttendanceRepo = new MeetingAttendanceRepo(context);
            attendances = meetingAttendanceRepo.getMeetingAttendanceForAllMembers(meeting.getMeetingId());
        }
    }

    protected void loadSaving(){
        if(this.meetingLoaded) {
            MeetingSavingRepo meetingSavingRepo = new MeetingSavingRepo(context);
            savings = meetingSavingRepo.getMeetingSavingsForAllMembers(meeting.getMeetingId());
        }
    }

    protected void loadLoanIssue(){
        if(this.meetingLoaded) {
            MeetingLoanIssuedRepo meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(context);
            loanIssues = meetingLoanIssuedRepo.getMeetingLoansForAllMembers(meeting.getMeetingId());
        }
    }

    protected void loadLoanRepayment(){
        if(this.meetingLoaded){
            MeetingLoanRepaymentRepo meetingLoanRepaymentRepo = new MeetingLoanRepaymentRepo(context);
            loanRepayments = meetingLoanRepaymentRepo.getMeetingRepaymentsForAllMembers(meeting.getMeetingId());
        }
    }

    protected void loadFines(){
        if(this.meetingLoaded){
            MeetingFineRepo meetingFineRepo = new MeetingFineRepo(context);
            fines = meetingFineRepo.getMeetingFinesForAllMembers(meeting.getMeetingId());
        }
    }

    protected double getTotalFinesPaid(){
        MeetingFineRepo meetingFineRepo = new MeetingFineRepo(context);
        return meetingFineRepo.getTotalFinesPaidInThisMeeting(meeting.getMeetingId());
    }

    protected double getMembersPresent(){
        MeetingAttendanceRepo meetingAttendanceRepo = new MeetingAttendanceRepo(context);
        return meetingAttendanceRepo.getAttendanceCountByMeetingId(meeting.getMeetingId());
    }

    protected double getTotalSavings(){
        MeetingSavingRepo meetingSavingRepo = new MeetingSavingRepo(context);
        return meetingSavingRepo.getTotalSavingsInMeeting(meeting.getMeetingId());
    }

    protected double getTotalLoansPaid(){
        MeetingLoanRepaymentRepo meetingLoanRepaymentRepo = new MeetingLoanRepaymentRepo(context);
        return meetingLoanRepaymentRepo.getTotalLoansRepaidInMeeting(meeting.getMeetingId());
    }

    protected double getTotalLoansIssued(){
        MeetingLoanIssuedRepo meetingLoanIssuedRepo = new MeetingLoanIssuedRepo(context);
        return meetingLoanIssuedRepo.getTotalLoansIssuedInMeeting(meeting.getMeetingId());
    }
}