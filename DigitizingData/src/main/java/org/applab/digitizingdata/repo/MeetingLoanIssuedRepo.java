package org.applab.digitizingdata.repo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.applab.digitizingdata.datatransformation.LoanDataTransferRecord;
import org.applab.digitizingdata.domain.model.Meeting;
import org.applab.digitizingdata.domain.model.MeetingLoanIssued;
import org.applab.digitizingdata.domain.model.Member;
import org.applab.digitizingdata.domain.schema.LoanIssueSchema;
import org.applab.digitizingdata.domain.schema.LoanRepaymentSchema;
import org.applab.digitizingdata.domain.schema.MeetingSchema;
import org.applab.digitizingdata.helpers.DatabaseHandler;
import org.applab.digitizingdata.helpers.MemberLoanIssueRecord;
import org.applab.digitizingdata.helpers.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Moses on 7/9/13.
 */
public class MeetingLoanIssuedRepo {
    private Context context;

    public MeetingLoanIssuedRepo() {

    }

    public MeetingLoanIssuedRepo(Context context) {
        this.context = context;
    }

    public double getTotalOutstandingLoansInCycle(int cycleId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double outstandingLoans = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS OutstandingLoans FROM %s " +
                            " WHERE (%s IS NULL OR %s=0) AND %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    LoanIssueSchema.COL_LI_BALANCE, LoanIssueSchema.getTableName(), LoanIssueSchema.COL_LI_IS_CLEARED,
                    LoanIssueSchema.COL_LI_IS_CLEARED, LoanIssueSchema.COL_LI_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId
            );
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                outstandingLoans = cursor.getDouble(cursor.getColumnIndex("OutstandingLoans"));
            }

            return outstandingLoans;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getTotalOutstandingLoansInCycle", ex.getMessage());
            return 0;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public double getTotalLoansIssuedInCycle(int cycleId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double loansIssued = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalIssues FROM %s WHERE %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT, LoanIssueSchema.getTableName(),
                    LoanIssueSchema.COL_LI_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                loansIssued = cursor.getDouble(cursor.getColumnIndex("TotalIssues"));
            }

            return loansIssued;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getTotalLoansIssuedInCycle", ex.getMessage());
            return 0;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public double getTotalLoansIssuedToMemberInCycle(int cycleId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double loansIssued = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalIssues FROM %s WHERE %s=%d AND %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT, LoanIssueSchema.getTableName(),
                    LoanIssueSchema.COL_LI_MEMBER_ID, memberId,
                    LoanIssueSchema.COL_LI_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                loansIssued = cursor.getDouble(cursor.getColumnIndex("TotalIssues"));
            }

            return loansIssued;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getTotalLoansIssuedToMemberInCycle", ex.getMessage());
            return 0;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public MeetingLoanIssued getTotalOutstandingLoansByMemberInCycle(int cycleId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double loanBalance = 0.00;
        MeetingLoanIssued loan = null;


        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT SUM(%s) AS LoanBalance, %s AS NextDueDate, %s AS DateCleared, %s AS IsCleared FROM %s WHERE %s=%d AND %s IN (SELECT %s FROM %s WHERE %s=%d)",
                    LoanIssueSchema.COL_LI_BALANCE,
                    LoanIssueSchema.COL_LI_DATE_DUE,
                    LoanIssueSchema.COL_LI_DATE_CLEARED,
                    LoanIssueSchema.COL_LI_IS_CLEARED,
                    LoanIssueSchema.getTableName(),
                    LoanIssueSchema.COL_LI_MEMBER_ID, memberId,
                    LoanIssueSchema.COL_LI_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                loan = new MeetingLoanIssued();
                loan.setLoanBalance(cursor.getDouble(cursor.getColumnIndex("LoanBalance")));
                if (cursor.getString(cursor.getColumnIndex("NextDueDate")) != null) {
                    Date dateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("NextDueDate")));
                    loan.setDateDue(dateDue);
                }
                if (cursor.getString(cursor.getColumnIndex("DateCleared")) != null) {
                    Date dateCleared = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateCleared")));
                    loan.setDateCleared(dateCleared);
                }
                loan.setCleared((cursor.getInt(cursor.getColumnIndex("IsCleared")) == 1));
            }

            return loan;

            //return loanBalance;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getTotalOutstandingLoansByMemberInCycle", ex.getMessage());
            return null;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public MeetingLoanIssued getOutstandingLoansByMemberInCycle(int cycleId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        MeetingLoanIssued loanIssued = new MeetingLoanIssued();

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT SUM(%s) AS LoanBalance, %s AS Interest, %s AS Principle FROM %s WHERE %s=%d AND %s IN (SELECT %s FROM %s WHERE %s=%d) ORDER BY %s DESC ",
                    LoanIssueSchema.COL_LI_BALANCE,
                    LoanIssueSchema.COL_LI_INTEREST_AMOUNT,
                    LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT,
                    LoanIssueSchema.getTableName(),
                    LoanIssueSchema.COL_LI_MEMBER_ID, memberId,
                    LoanIssueSchema.COL_LI_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId, LoanIssueSchema.COL_LI_MEETING_ID);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                loanIssued.setLoanBalance(cursor.getDouble(cursor.getColumnIndex("LoanBalance")));
                loanIssued.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("Interest")));
                loanIssued.setPrincipalAmount(cursor.getDouble(cursor.getColumnIndex("Principle")));

            }

            return loanIssued;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getTotalOutstandingLoansByMemberInCycle", ex.getMessage());
            return loanIssued;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public ArrayList<MeetingLoanIssued> getOutstandingLoansListByMemberInCycle(int cycleId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<MeetingLoanIssued> loanIssuedList = new ArrayList<MeetingLoanIssued>();
        MeetingLoanIssued loanIssued = new MeetingLoanIssued();

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT %s AS LoanBalance, %s AS Comment FROM %s WHERE %s=%d AND %s IN (SELECT %s FROM %s WHERE %s=%d) ORDER BY %s DESC",
                    LoanIssueSchema.COL_LI_BALANCE,
                    LoanIssueSchema.COL_LI_COMMENT,
                    LoanIssueSchema.getTableName(),
                    LoanIssueSchema.COL_LI_MEMBER_ID, memberId,
                    LoanIssueSchema.COL_LI_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId,
                    LoanIssueSchema.COL_LI_LOAN_ID);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                loanIssued = new MeetingLoanIssued();
                loanIssued.setLoanBalance(cursor.getDouble(cursor.getColumnIndex("LoanBalance")));
                loanIssued.setComment(cursor.getString((cursor.getColumnIndex("Comment"))));
                loanIssuedList.add(loanIssued);

            }
            return loanIssuedList;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getOutstandingLoansByMemberInCycle", ex.getMessage());
            return null;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public double getTotalLoansIssuedInMeeting(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double loansIssued = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT SUM(%s) AS TotalIssues FROM %s WHERE %s=%d",
                    LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT, LoanIssueSchema.getTableName(),
                    LoanIssueSchema.COL_LI_MEETING_ID, meetingId);
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                loansIssued = cursor.getDouble(cursor.getColumnIndex("TotalIssues"));
            }
            return loansIssued;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getTotalLoansIssuedInMeeting", ex.getMessage());
            return 0;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public double getTotalLoansIssuedToMemberInMeeting(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        double loansIssued = 0.00;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String sumQuery = String.format("SELECT  SUM(%s) AS TotalIssues FROM %s WHERE %s=%d AND %s=%d",
                    LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT, LoanIssueSchema.getTableName(),
                    LoanIssueSchema.COL_LI_MEETING_ID, meetingId,
                    LoanIssueSchema.COL_LI_MEMBER_ID, memberId
            );
            cursor = db.rawQuery(sumQuery, null);

            if (cursor != null && cursor.moveToFirst()) {
                loansIssued = cursor.getDouble(cursor.getColumnIndex("TotalIssues"));
            }

            return loansIssued;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getTotalLoansIssuedInMeeting", ex.getMessage());
            return 0;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * checks whether a Loan Number has already been used.
     *
     * @param loanNo
     * @return
     */
    public boolean validateLoanNumber(int loanNo) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            if (loanNo <= 0) {
                return false;
            }
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s FROM %s WHERE %s=%d",
                    LoanIssueSchema.COL_LI_LOAN_ID, LoanIssueSchema.getTableName(),
                    LoanIssueSchema.COL_LI_LOAN_NO, loanNo);
            cursor = db.rawQuery(query, null);

            return !(cursor != null && cursor.moveToFirst());

        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.validateLoanNumber", ex.getMessage());
            return false;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * checks whether a Loan Number has already been used.
     * Here I include MeetingId and MemberId to support validation during edit
     *
     * @param loanNo
     * @return
     */
    public boolean validateLoanNumber(int loanNo, int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;

        try {
            if (loanNo <= 0) {
                return false;
            }

            //Get the Loan Id
            int loanId = getMemberLoanId(meetingId, memberId);

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s FROM %s WHERE %s=%d AND %s <> %d",
                    LoanIssueSchema.COL_LI_LOAN_ID, LoanIssueSchema.getTableName(),
                    LoanIssueSchema.COL_LI_LOAN_NO, loanNo, LoanIssueSchema.COL_LI_LOAN_ID, loanId);
            cursor = db.rawQuery(query, null);

            return !(cursor != null && cursor.moveToFirst());

        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.validateLoanNumber", ex.getMessage());
            return false;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Important when Updating a Record on the Loan Issue History Screen
     *
     * @param meetingId
     * @param memberId
     * @return
     */
    public int getMemberLoanId(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int loanId = 0;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s FROM %s WHERE %s=%d AND %s=%d ORDER BY %s DESC LIMIT 1",
                    LoanIssueSchema.COL_LI_LOAN_ID, LoanIssueSchema.getTableName(),
                    LoanIssueSchema.COL_LI_MEETING_ID, meetingId,
                    LoanIssueSchema.COL_LI_MEMBER_ID, memberId, LoanIssueSchema.COL_LI_LOAN_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                loanId = cursor.getInt(cursor.getColumnIndex(LoanIssueSchema.COL_LI_LOAN_ID));
            }
            return loanId;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getMemberLoanId", ex.getMessage());
            return loanId;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    /**
     * Important when Updating a Record on the Loan Issue History Screen
     *
     * @param memberId
     * @return
     */
    public int getMemberLoanId(int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int loanId = 0;

        try {
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT %s FROM %s WHERE %s=%d ORDER BY %s DESC LIMIT 1",
                    LoanIssueSchema.COL_LI_LOAN_ID, LoanIssueSchema.getTableName(),
                    LoanIssueSchema.COL_LI_MEMBER_ID, memberId,
                    LoanIssueSchema.COL_LI_LOAN_ID);
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                loanId = cursor.getInt(cursor.getColumnIndex(LoanIssueSchema.COL_LI_LOAN_ID));
            }
            return loanId;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getMemberLoanId", ex.getMessage());
            return loanId;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public boolean saveMemberLoanIssue(int meetingId, int memberId, int loanNo, double amount, double interest, double balance, Date dateDue, String comment, boolean isUpdate) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        int loanId = 0;
        try {
            // Check if exists and do an Update
            loanId = getMemberLoanId(memberId);
            // if (loanId > 0) {

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            if (isUpdate) {
                values.put(LoanIssueSchema.COL_LI_BALANCE, balance);

                // Ensure Total Repaid is not zero.
                values.put(LoanIssueSchema.COL_LI_TOTAL_REPAID, getLoanIssuedByLoanId(loanId).getTotalRepaid());
                performUpdate = true;
            } else {
                values.put(LoanIssueSchema.COL_LI_MEETING_ID, meetingId);

                // Ensure Total Repaid is Zero.
                values.put(LoanIssueSchema.COL_LI_TOTAL_REPAID, 0);

                // Get the total Loan Amount
                double totalLoan = amount + interest;

                //Set Balance to be Principal Amount + Interest Amount
                values.put(LoanIssueSchema.COL_LI_BALANCE, totalLoan);
            }

            values.put(LoanIssueSchema.COL_LI_MEMBER_ID, memberId);
            values.put(LoanIssueSchema.COL_LI_LOAN_NO, loanNo);
            values.put(LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT, amount);
            values.put(LoanIssueSchema.COL_LI_INTEREST_AMOUNT, interest);
            values.put(LoanIssueSchema.COL_LI_COMMENT, comment);

            //The Date Due
            Date dtDateDue = dateDue;
            if (dateDue == null) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.WEEK_OF_YEAR, 4);
                dtDateDue = cal.getTime();
            }
            values.put(LoanIssueSchema.COL_LI_DATE_DUE, Utils.formatDateToSqlite(dtDateDue));


            // Inserting or UpdatingRow
            long retVal = -1;
            if (performUpdate) {
                Log.d("MLIR", "performing update");
                // updating row
                retVal = db.update(LoanIssueSchema.getTableName(), values, LoanIssueSchema.COL_LI_LOAN_ID + " = ?",
                        new String[]{String.valueOf(loanId)});
            } else {
                Log.d("MLIR", "performing insert");
                retVal = db.insert(LoanIssueSchema.getTableName(), null, values);
            }

            return retVal != -1;
        } catch (Exception ex) {
            Log.e("MemberLoanIssuedRepo.saveMemberLoanIssue", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean saveMemberLoanIssue(int meetingId, int memberId, int loanNo, double amount, double loanBalance, Date dateDue) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;
        int loanId = 0;
        try {
            //Check if exists and do an Update
            loanId = getMemberLoanId(meetingId, memberId);
            if (loanId > 0) {
                performUpdate = true;
            }

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(LoanIssueSchema.COL_LI_MEETING_ID, meetingId);
            values.put(LoanIssueSchema.COL_LI_MEMBER_ID, memberId);
            values.put(LoanIssueSchema.COL_LI_LOAN_NO, loanNo);
            values.put(LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT, amount);
            values.put(LoanIssueSchema.COL_LI_INTEREST_AMOUNT, 0.0);


            //The Date Due
            Date dtDateDue = dateDue;
            if (dateDue == null) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.WEEK_OF_YEAR, 4);
                dtDateDue = cal.getTime();
            }
            values.put(LoanIssueSchema.COL_LI_DATE_DUE, Utils.formatDateToSqlite(dtDateDue));

            //Set Balance to be the value that was pushed in. Useful when Balance is explicitly determined
            values.put(LoanIssueSchema.COL_LI_BALANCE, loanBalance);

            //Ensure Total Repaid is Zero.
            values.put(LoanIssueSchema.COL_LI_TOTAL_REPAID, 0);

            // Inserting or UpdatingRow
            long retVal = -1;
            if (performUpdate) {
                // updating row
                retVal = db.update(LoanIssueSchema.getTableName(), values, LoanIssueSchema.COL_LI_LOAN_ID + " = ?",
                        new String[]{String.valueOf(loanId)});
            } else {
                retVal = db.insert(LoanIssueSchema.getTableName(), null, values);
            }

            return retVal != -1;
        } catch (Exception ex) {
            Log.e("MemberLoanIssuedRepo.saveMemberLoanIssue", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public ArrayList<MemberLoanIssueRecord> getLoansIssuedToMemberInCycle(int cycleId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int _isCleared = 1;
        ArrayList<MemberLoanIssueRecord> loansIssued;

        try {
            loansIssued = new ArrayList<MemberLoanIssueRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  L.%s AS LoanId, M.%s AS MeetingDate, L.%s AS PrincipalAmount, " +
                            "L.%s AS LoanNo, L.%s AS Balance, L.%s AS IsCleared, L.%s AS DateCleared, L.%s AS DateDue, L.%s AS InterestAmount, " +
                            "(SELECT R.%s FROM %s AS R WHERE R.%s=L.%s ORDER BY R.%s DESC LIMIT 1) AS LastRepaymentComment " +
                            " FROM %s AS L INNER JOIN %s AS M ON L.%s=M.%s WHERE L.%s=%d AND L.%s=%d AND L.%s IN (SELECT %s FROM %s WHERE %s=%d) ORDER BY L.%s DESC",
                    LoanIssueSchema.COL_LI_LOAN_ID, MeetingSchema.COL_MT_MEETING_DATE, LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanIssueSchema.COL_LI_BALANCE, LoanIssueSchema.COL_LI_IS_CLEARED, LoanIssueSchema.COL_LI_DATE_CLEARED,
                    LoanIssueSchema.COL_LI_DATE_DUE, LoanIssueSchema.COL_LI_INTEREST_AMOUNT,
                    LoanRepaymentSchema.COL_LR_COMMENTS, LoanRepaymentSchema.getTableName(),
                    LoanRepaymentSchema.COL_LR_LOAN_ID, LoanIssueSchema.COL_LI_LOAN_ID, LoanRepaymentSchema.COL_LR_REPAYMENT_ID,
                    LoanIssueSchema.getTableName(), MeetingSchema.getTableName(),
                    LoanIssueSchema.COL_LI_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    LoanIssueSchema.COL_LI_MEMBER_ID, memberId,
                    LoanIssueSchema.COL_LI_IS_CLEARED, _isCleared,
                    LoanIssueSchema.COL_LI_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID, MeetingSchema.getTableName(), MeetingSchema.COL_MT_CYCLE_ID, cycleId,
                    LoanIssueSchema.COL_LI_LOAN_ID
            );
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MemberLoanIssueRecord loanRecord = new MemberLoanIssueRecord();
                    if (cursor.getString(cursor.getColumnIndex("DateDue")) != null) {
                        Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("MeetingDate")));
                        loanRecord.setMeetingDate(meetingDate);
                    }
                    loanRecord.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                    loanRecord.setPrincipalAmount(cursor.getDouble(cursor.getColumnIndex("PrincipalAmount")));
                    loanRecord.setLoanNo(cursor.getInt(cursor.getColumnIndex("LoanNo")));
                    loanRecord.setBalance(cursor.getDouble(cursor.getColumnIndex("Balance")));
                    loanRecord.setCleared((cursor.getInt(cursor.getColumnIndex("IsCleared")) == 1) ? true : false);
                    if (cursor.getString(cursor.getColumnIndex("DateCleared")) != null) {
                        Date dateCleared = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateCleared")));
                        loanRecord.setDateCleared(dateCleared);
                    }
                    if (cursor.getString(cursor.getColumnIndex("DateDue")) != null) {
                        Date dateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateDue")));
                        loanRecord.setDateDue(dateDue);
                    }
                    loanRecord.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("InterestAmount")));

                    loanRecord.setLastRepaymentComment(cursor.getString(cursor.getColumnIndex("LastRepaymentComment")));

                    loansIssued.add(loanRecord);
                } while (cursor.moveToNext());
            }
            return loansIssued;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getLoansIssuedToMemberInCycle", ex.getMessage());
            return null;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public ArrayList<MemberLoanIssueRecord> getAllLoansIssuedToMember(int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        int _isCleared = 1;
        ArrayList<MemberLoanIssueRecord> loansIssued;

        try {
            loansIssued = new ArrayList<MemberLoanIssueRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  L.%s AS LoanId, M.%s AS MeetingDate, L.%s AS PrincipalAmount, " +
                            "L.%s AS LoanNo, L.%s AS Balance, L.%s AS IsCleared, L.%s AS DateCleared, L.%s AS DateDue, L.%s AS InterestAmount, " +
                            "(SELECT R.%s FROM %s AS R WHERE R.%s=L.%s ORDER BY R.%s DESC LIMIT 1) AS LastRepaymentComment " +
                            " FROM %s AS L INNER JOIN %s AS M ON L.%s=M.%s WHERE L.%s=%d ORDER BY L.%s DESC",
                    LoanIssueSchema.COL_LI_LOAN_ID, MeetingSchema.COL_MT_MEETING_DATE, LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanIssueSchema.COL_LI_BALANCE, LoanIssueSchema.COL_LI_IS_CLEARED, LoanIssueSchema.COL_LI_DATE_CLEARED,
                    LoanIssueSchema.COL_LI_DATE_DUE, LoanIssueSchema.COL_LI_INTEREST_AMOUNT,
                    LoanRepaymentSchema.COL_LR_COMMENTS, LoanRepaymentSchema.getTableName(),
                    LoanRepaymentSchema.COL_LR_LOAN_ID, LoanIssueSchema.COL_LI_LOAN_ID, LoanRepaymentSchema.COL_LR_REPAYMENT_ID,
                    LoanIssueSchema.getTableName(), MeetingSchema.getTableName(),
                    LoanIssueSchema.COL_LI_MEETING_ID, MeetingSchema.COL_MT_MEETING_ID,
                    LoanIssueSchema.COL_LI_MEMBER_ID, memberId,
                    LoanIssueSchema.COL_LI_LOAN_ID
            );
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    MemberLoanIssueRecord loanRecord = new MemberLoanIssueRecord();
                    if (cursor.getString(cursor.getColumnIndex("DateDue")) != null) {
                        Date meetingDate = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("MeetingDate")));
                        loanRecord.setMeetingDate(meetingDate);
                    }
                    loanRecord.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                    loanRecord.setPrincipalAmount(cursor.getDouble(cursor.getColumnIndex("PrincipalAmount")));
                    loanRecord.setLoanNo(cursor.getInt(cursor.getColumnIndex("LoanNo")));
                    loanRecord.setBalance(cursor.getDouble(cursor.getColumnIndex("Balance")));
                    loanRecord.setCleared((cursor.getInt(cursor.getColumnIndex("IsCleared")) == 1) ? true : false);
                    if (cursor.getString(cursor.getColumnIndex("DateCleared")) != null) {
                        Date dateCleared = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateCleared")));
                        loanRecord.setDateCleared(dateCleared);
                    }
                    if (cursor.getString(cursor.getColumnIndex("DateDue")) != null) {
                        Date dateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateDue")));
                        loanRecord.setDateDue(dateDue);
                    }
                    loanRecord.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("InterestAmount")));

                    loanRecord.setLastRepaymentComment(cursor.getString(cursor.getColumnIndex("LastRepaymentComment")));

                    loansIssued.add(loanRecord);

                } while (cursor.moveToNext());
            }
            return loansIssued;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getLoansIssuedToMemberInCycle", ex.getMessage());
            return null;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public ArrayList<LoanDataTransferRecord> getMeetingLoansForAllMembers(int meetingId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        ArrayList<LoanDataTransferRecord> loansIssued;

        try {
            loansIssued = new ArrayList<LoanDataTransferRecord>();

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  %s AS LoanId, %s AS PrincipalAmount, %s AS IsWrittenOff, %s AS IsDefaulted, " +
                            "%s AS LoanNo, %s AS Balance, %s AS IsCleared, %s AS DateCleared, %s AS DateDue, %s AS InterestAmount, " +
                            " %s AS MemberId, %s AS TotalRepaid, %s AS Comments " +
                            " FROM %s WHERE %s=%d ORDER BY %s",
                    LoanIssueSchema.COL_LI_LOAN_ID, LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT, LoanIssueSchema.COL_LI_IS_WRITTEN_OFF,
                    LoanIssueSchema.COL_LI_IS_DEFAULTED, LoanIssueSchema.COL_LI_LOAN_NO, LoanIssueSchema.COL_LI_BALANCE, LoanIssueSchema.COL_LI_IS_CLEARED,
                    LoanIssueSchema.COL_LI_DATE_CLEARED, LoanIssueSchema.COL_LI_DATE_DUE, LoanIssueSchema.COL_LI_INTEREST_AMOUNT,
                    LoanIssueSchema.COL_LI_MEMBER_ID, LoanIssueSchema.COL_LI_TOTAL_REPAID, LoanIssueSchema.COL_LI_COMMENT,
                    LoanIssueSchema.getTableName(), LoanIssueSchema.COL_LI_MEETING_ID, meetingId, LoanIssueSchema.COL_LI_LOAN_ID
            );
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    LoanDataTransferRecord loanRecord = new LoanDataTransferRecord();
                    loanRecord.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                    loanRecord.setMeetingId(meetingId);
                    loanRecord.setMemberId(cursor.getInt(cursor.getColumnIndex("MemberId")));
                    loanRecord.setPrincipalAmount(cursor.getDouble(cursor.getColumnIndex("PrincipalAmount")));
                    loanRecord.setLoanNo(cursor.getInt(cursor.getColumnIndex("LoanNo")));
                    loanRecord.setLoanBalance(cursor.getDouble(cursor.getColumnIndex("Balance")));
                    loanRecord.setCleared((cursor.getInt(cursor.getColumnIndex("IsCleared")) == 1) ? true : false);
                    if (cursor.getString(cursor.getColumnIndex("DateCleared")) != null) {
                        Date dateCleared = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateCleared")));
                        loanRecord.setDateCleared(dateCleared);
                    }
                    if (cursor.getString(cursor.getColumnIndex("DateDue")) != null) {
                        Date dateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateDue")));
                        loanRecord.setDateDue(dateDue);
                    }
                    loanRecord.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("InterestAmount")));
                    loanRecord.setDefaulted((cursor.getInt(cursor.getColumnIndex("IsDefaulted")) == 1) ? true : false);
                    loanRecord.setWrittenOff((cursor.getInt(cursor.getColumnIndex("IsWrittenOff")) == 1) ? true : false);
                    loanRecord.setTotalRepaid(cursor.getDouble(cursor.getColumnIndex("TotalRepaid")));
                    loanRecord.setComments(cursor.getString(cursor.getColumnIndex("Comments")));

                    loansIssued.add(loanRecord);

                } while (cursor.moveToNext());
            }
            return loansIssued;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getMeetingLoansForAllMembers", ex.getMessage());
            return null;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public MeetingLoanIssued getMostRecentLoanIssuedToMember(int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        MeetingLoanIssued loan = null;

        try {

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  L.%s AS LoanId, L.%s AS MeetingId, L.%s AS PrincipalAmount, L.%s AS InterestAmount, " +
                            "L.%s AS LoanNo, L.%s AS Balance, L.%s AS TotalRepaid, L.%s AS IsCleared, L.%s AS DateCleared, L.%s AS DateDue " +
                            " FROM %s AS L WHERE L.%s=%d AND (L.%s IS NULL OR L.%s = 0) ORDER BY L.%s DESC LIMIT 1",
                    LoanIssueSchema.COL_LI_LOAN_ID, LoanIssueSchema.COL_LI_MEETING_ID, LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT, LoanIssueSchema.COL_LI_INTEREST_AMOUNT,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanIssueSchema.COL_LI_BALANCE, LoanIssueSchema.COL_LI_TOTAL_REPAID,
                    LoanIssueSchema.COL_LI_IS_CLEARED, LoanIssueSchema.COL_LI_DATE_CLEARED, LoanIssueSchema.COL_LI_DATE_DUE,
                    LoanIssueSchema.getTableName(), LoanIssueSchema.COL_LI_MEMBER_ID, memberId, LoanIssueSchema.COL_LI_IS_CLEARED, LoanIssueSchema.COL_LI_IS_CLEARED,
                    LoanIssueSchema.COL_LI_LOAN_ID
            );
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {

                loan = new MeetingLoanIssued();

                loan.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                Meeting meeting = new Meeting();
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex("MeetingId")));
                loan.setMeeting(meeting);
                loan.setPrincipalAmount(cursor.getDouble(cursor.getColumnIndex("PrincipalAmount")));
                loan.setLoanNo(cursor.getInt(cursor.getColumnIndex("LoanNo")));
                loan.setLoanBalance(cursor.getDouble(cursor.getColumnIndex("Balance")));
                loan.setTotalRepaid(cursor.getDouble(cursor.getColumnIndex("TotalRepaid")));
                loan.setCleared((cursor.getInt(cursor.getColumnIndex("IsCleared")) == 1) ? true : false);
                if (cursor.getString(cursor.getColumnIndex("DateCleared")) != null) {
                    Date dateCleared = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateCleared")));
                    loan.setDateCleared(dateCleared);
                }
                if (cursor.getString(cursor.getColumnIndex("DateDue")) != null) {
                    Date dateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateDue")));
                    loan.setDateDue(dateDue);
                }
                loan.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("InterestAmount")));

                Member member = new Member();
                member.setMemberId(memberId);
                loan.setMember(member);

            }
            return loan;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getMostRecentLoanIssuedToMember", ex.getMessage());
            return null;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public MeetingLoanIssued getAllMostRecentLoanIssuedToMember(int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        MeetingLoanIssued loan = null;

        try {

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  L.%s AS LoanId, L.%s AS MeetingId, L.%s AS PrincipalAmount, L.%s AS InterestAmount, " +
                            "L.%s AS LoanNo, L.%s AS Balance, L.%s AS TotalRepaid, L.%s AS IsCleared, L.%s AS DateCleared, L.%s AS DateDue " +
                            " FROM %s AS L WHERE L.%s=%d ORDER BY L.%s DESC LIMIT 1",
                    LoanIssueSchema.COL_LI_LOAN_ID, LoanIssueSchema.COL_LI_MEETING_ID, LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT, LoanIssueSchema.COL_LI_INTEREST_AMOUNT,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanIssueSchema.COL_LI_BALANCE, LoanIssueSchema.COL_LI_TOTAL_REPAID,
                    LoanIssueSchema.COL_LI_IS_CLEARED, LoanIssueSchema.COL_LI_DATE_CLEARED, LoanIssueSchema.COL_LI_DATE_DUE,
                    LoanIssueSchema.getTableName(), LoanIssueSchema.COL_LI_MEMBER_ID, memberId, LoanIssueSchema.COL_LI_LOAN_ID
            );
            cursor = db.rawQuery(query, null);

            if (cursor != null && cursor.moveToFirst()) {

                loan = new MeetingLoanIssued();

                loan.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                Meeting meeting = new Meeting();
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex("MeetingId")));
                loan.setMeeting(meeting);
                loan.setPrincipalAmount(cursor.getDouble(cursor.getColumnIndex("PrincipalAmount")));
                loan.setLoanNo(cursor.getInt(cursor.getColumnIndex("LoanNo")));
                loan.setLoanBalance(cursor.getDouble(cursor.getColumnIndex("Balance")));
                loan.setTotalRepaid(cursor.getDouble(cursor.getColumnIndex("TotalRepaid")));
                loan.setCleared((cursor.getInt(cursor.getColumnIndex("IsCleared")) == 1) ? true : false);
                if (cursor.getString(cursor.getColumnIndex("DateCleared")) != null) {
                    Date dateCleared = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateCleared")));
                    loan.setDateCleared(dateCleared);
                }
                if (cursor.getString(cursor.getColumnIndex("DateDue")) != null) {
                    Date dateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateDue")));
                    loan.setDateDue(dateDue);
                }
                loan.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("InterestAmount")));

                Member member = new Member();
                member.setMemberId(memberId);
                loan.setMember(member);

            }
            return loan;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getMostRecentLoanIssuedToMember", ex.getMessage());
            return null;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public MeetingLoanIssued getLoanIssuedToMemberInMeeting(int meetingId, int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        MeetingLoanIssued loan = null;

        try {

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  L.%s AS LoanId, L.%s AS MeetingId, L.%s AS PrincipalAmount, L.%s AS InterestAmount, " +
                            "L.%s AS LoanNo, L.%s AS Balance, L.%s AS TotalRepaid, L.%s AS Comment, L.%s AS IsCleared, L.%s AS DateCleared, L.%s AS DateDue " +
                            " FROM %s AS L WHERE L.%s=%d AND L.%s=%d AND (L.%s IS NULL OR L.%s = 0) ORDER BY L.%s DESC LIMIT 1",
                    LoanIssueSchema.COL_LI_LOAN_ID, LoanIssueSchema.COL_LI_MEETING_ID, LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT, LoanIssueSchema.COL_LI_INTEREST_AMOUNT,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanIssueSchema.COL_LI_BALANCE, LoanIssueSchema.COL_LI_TOTAL_REPAID,
                    LoanIssueSchema.COL_LI_COMMENT, LoanIssueSchema.COL_LI_IS_CLEARED, LoanIssueSchema.COL_LI_DATE_CLEARED, LoanIssueSchema.COL_LI_DATE_DUE,
                    LoanIssueSchema.getTableName(), LoanIssueSchema.COL_LI_MEETING_ID, meetingId,
                    LoanIssueSchema.COL_LI_MEMBER_ID, memberId, LoanIssueSchema.COL_LI_IS_CLEARED, LoanIssueSchema.COL_LI_IS_CLEARED,
                    LoanIssueSchema.COL_LI_LOAN_ID
            );

            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {

                loan = new MeetingLoanIssued();

                loan.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                Meeting meeting = new Meeting();
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex("MeetingId")));
                loan.setMeeting(meeting);
                loan.setPrincipalAmount(cursor.getDouble(cursor.getColumnIndex("PrincipalAmount")));
                loan.setLoanNo(cursor.getInt(cursor.getColumnIndex("LoanNo")));
                loan.setLoanBalance(cursor.getDouble(cursor.getColumnIndex("Balance")));
                loan.setTotalRepaid(cursor.getDouble(cursor.getColumnIndex("TotalRepaid")));
                loan.setComment(cursor.getString(cursor.getColumnIndex("Comment")));
                loan.setCleared((cursor.getInt(cursor.getColumnIndex("IsCleared")) == 1) ? true : false);
                if (cursor.getString(cursor.getColumnIndex("DateCleared")) != null) {
                    Date dateCleared = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateCleared")));
                    loan.setDateCleared(dateCleared);
                }
                if (cursor.getString(cursor.getColumnIndex("DateDue")) != null) {
                    Date dateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateDue")));
                    loan.setDateDue(dateDue);
                }
                loan.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("InterestAmount")));

                Member member = new Member();
                member.setMemberId(memberId);
                loan.setMember(member);

            }
            return loan;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("MeetingLoanIssuedRepo.getMostRecentLoanIssuedToMember", ex.getMessage());
            return null;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public MeetingLoanIssued getUnclearedLoanIssuedToMember(int memberId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        MeetingLoanIssued loan = null;

        try {

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  L.%s AS LoanId, L.%s AS MeetingId, L.%s AS PrincipalAmount, L.%s AS InterestAmount, " +
                            "L.%s AS LoanNo, L.%s AS Balance, L.%s AS TotalRepaid, L.%s AS Comment, L.%s AS IsCleared, L.%s AS DateCleared, L.%s AS DateDue " +
                            " FROM %s AS L WHERE L.%s=%d AND (L.%s IS NULL OR L.%s = 0) ORDER BY L.%s DESC LIMIT 1",
                    LoanIssueSchema.COL_LI_LOAN_ID, LoanIssueSchema.COL_LI_MEETING_ID, LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT, LoanIssueSchema.COL_LI_INTEREST_AMOUNT,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanIssueSchema.COL_LI_BALANCE, LoanIssueSchema.COL_LI_TOTAL_REPAID,
                    LoanIssueSchema.COL_LI_COMMENT, LoanIssueSchema.COL_LI_IS_CLEARED, LoanIssueSchema.COL_LI_DATE_CLEARED, LoanIssueSchema.COL_LI_DATE_DUE,
                    LoanIssueSchema.getTableName(),
                    LoanIssueSchema.COL_LI_MEMBER_ID, memberId, LoanIssueSchema.COL_LI_IS_CLEARED, LoanIssueSchema.COL_LI_IS_CLEARED,
                    LoanIssueSchema.COL_LI_LOAN_ID
            );

            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {

                loan = new MeetingLoanIssued();

                loan.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                Meeting meeting = new Meeting();
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex("MeetingId")));
                loan.setMeeting(meeting);
                loan.setPrincipalAmount(cursor.getDouble(cursor.getColumnIndex("PrincipalAmount")));
                loan.setLoanNo(cursor.getInt(cursor.getColumnIndex("LoanNo")));
                loan.setLoanBalance(cursor.getDouble(cursor.getColumnIndex("Balance")));
                loan.setTotalRepaid(cursor.getDouble(cursor.getColumnIndex("TotalRepaid")));
                loan.setComment(cursor.getString(cursor.getColumnIndex("Comment")));
                loan.setCleared((cursor.getInt(cursor.getColumnIndex("IsCleared")) == 1) ? true : false);
                if (cursor.getString(cursor.getColumnIndex("DateCleared")) != null) {
                    Date dateCleared = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateCleared")));
                    loan.setDateCleared(dateCleared);
                }
                if (cursor.getString(cursor.getColumnIndex("DateDue")) != null) {
                    Date dateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateDue")));
                    loan.setDateDue(dateDue);
                }
                loan.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("InterestAmount")));

                Member member = new Member();
                member.setMemberId(memberId);
                loan.setMember(member);
            }
            return loan;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e("MeetingLoanIssuedRepo.getUnclearedLoanIssuedToMember", ex.getMessage());
            return null;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public MeetingLoanIssued getLoanIssuedByLoanId(int loanId) {
        SQLiteDatabase db = null;
        Cursor cursor = null;
        MeetingLoanIssued loan = null;

        try {

            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            String query = String.format("SELECT  L.%s AS LoanId, L.%s AS MeetingId, L.%s AS MemberId, L.%s AS PrincipalAmount, L.%s AS InterestAmount, " +
                            "L.%s AS LoanNo, L.%s AS Balance, L.%s AS TotalRepaid, L.%s AS IsCleared, L.%s AS DateCleared, L.%s AS DateDue " +
                            " FROM %s AS L WHERE L.%s=%d LIMIT 1",
                    LoanIssueSchema.COL_LI_LOAN_ID, LoanIssueSchema.COL_LI_MEETING_ID, LoanIssueSchema.COL_LI_MEMBER_ID,
                    LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT, LoanIssueSchema.COL_LI_INTEREST_AMOUNT,
                    LoanIssueSchema.COL_LI_LOAN_NO, LoanIssueSchema.COL_LI_BALANCE, LoanIssueSchema.COL_LI_TOTAL_REPAID,
                    LoanIssueSchema.COL_LI_IS_CLEARED, LoanIssueSchema.COL_LI_DATE_CLEARED, LoanIssueSchema.COL_LI_DATE_DUE,
                    LoanIssueSchema.getTableName(), LoanIssueSchema.COL_LI_LOAN_ID, loanId
            );
            cursor = db.rawQuery(query, null);
            if (cursor != null && cursor.moveToFirst()) {

                loan = new MeetingLoanIssued();

                loan.setLoanId(cursor.getInt(cursor.getColumnIndex("LoanId")));
                Meeting meeting = new Meeting();
                meeting.setMeetingId(cursor.getInt(cursor.getColumnIndex("MeetingId")));
                loan.setMeeting(meeting);
                loan.setPrincipalAmount(cursor.getDouble(cursor.getColumnIndex("PrincipalAmount")));
                loan.setLoanNo(cursor.getInt(cursor.getColumnIndex("LoanNo")));
                loan.setLoanBalance(cursor.getDouble(cursor.getColumnIndex("Balance")));
                loan.setTotalRepaid(cursor.getDouble(cursor.getColumnIndex("TotalRepaid")));
                loan.setCleared((cursor.getInt(cursor.getColumnIndex("IsCleared")) == 1) ? true : false);
                if (cursor.getString(cursor.getColumnIndex("DateCleared")) != null) {
                    Date dateCleared = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateCleared")));
                    loan.setDateCleared(dateCleared);
                }
                if (cursor.getString(cursor.getColumnIndex("DateDue")) != null) {
                    Date dateDue = Utils.getDateFromSqlite(cursor.getString(cursor.getColumnIndex("DateDue")));
                    loan.setDateDue(dateDue);
                }
                loan.setInterestAmount(cursor.getDouble(cursor.getColumnIndex("InterestAmount")));

                Member member = new Member();
                member.setMemberId(cursor.getInt(cursor.getColumnIndex("MemberId")));
                loan.setMember(member);
            }
            return loan;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.getLoanIssuedByLoanId", ex.getMessage());
            return null;
        } finally {

            if (cursor != null) {
                cursor.close();
            }

            if (db != null) {
                db.close();
            }
        }
    }

    public boolean updateMemberLoanBalancesWithMeetingDate(int loanId, double totalRepaid, double balance, Date newDateDue, String meetingDate) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;

        try {
            //TODO: Use a direct query to update the balances on the DB
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(LoanIssueSchema.COL_LI_BALANCE, balance);
            values.put(LoanIssueSchema.COL_LI_TOTAL_REPAID, totalRepaid);
            //If the loan has been cleared, then the newDateDue will be null
            if (null != newDateDue) {
                values.put(LoanIssueSchema.COL_LI_DATE_DUE, Utils.formatDateToSqlite(newDateDue));
            } else {
                //I use ContentValues.putNull(sKey) but I can just leave this line out
                values.putNull(LoanIssueSchema.COL_LI_DATE_DUE);
            }


            //Determine whether to flag the loan as cleared
            if (balance <= 0) {
                values.put(LoanIssueSchema.COL_LI_IS_CLEARED, 1);
                values.put(LoanIssueSchema.COL_LI_DATE_CLEARED, Utils.formatDateToSqlite(Utils.getDateFromString(meetingDate, Utils.OTHER_DATE_FIELD_FORMAT)));
                Log.d("MLIR", Utils.formatDateToSqlite(Utils.getDateFromString(meetingDate, Utils.OTHER_DATE_FIELD_FORMAT)));
            } else{
                values.put(LoanIssueSchema.COL_LI_IS_CLEARED, 0);
                values.putNull(LoanIssueSchema.COL_LI_DATE_CLEARED);

            }

            // Inserting or UpdatingRow
            long retVal = -1;

            // updating row
            retVal = db.update(LoanIssueSchema.getTableName(), values, LoanIssueSchema.COL_LI_LOAN_ID + " = ?",
                    new String[]{String.valueOf(loanId)});

            return retVal != -1;
        } catch (Exception ex) {
            Log.e("MemberLoanIssuedRepo.updateMemberLoanBalances", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public boolean updateMemberLoanBalances(int loanId, double totalRepaid, double balance, Date newDateDue) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;

        try {
            //TODO: Use a direct query to update the balances on the DB
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();

            values.put(LoanIssueSchema.COL_LI_BALANCE, balance);
            values.put(LoanIssueSchema.COL_LI_TOTAL_REPAID, totalRepaid);
            //If the loan has been cleared, then the newDateDue will be null
            if (null != newDateDue) {
                values.put(LoanIssueSchema.COL_LI_DATE_DUE, Utils.formatDateToSqlite(newDateDue));
            } else {
                //I use ContentValues.putNull(sKey) but I can just leave this line out
                values.putNull(LoanIssueSchema.COL_LI_DATE_DUE);
            }
            //Determine whether to flag the loan as cleared
            if (balance <= 0) {
                values.put(LoanIssueSchema.COL_LI_IS_CLEARED, 1);
                values.put(LoanIssueSchema.COL_LI_DATE_CLEARED, Utils.formatDateToSqlite(new Date()));
            }

            // Inserting or UpdatingRow
            long retVal = -1;

            // updating row
            retVal = db.update(LoanIssueSchema.getTableName(), values, LoanIssueSchema.COL_LI_LOAN_ID + " = ?",
                    new String[]{String.valueOf(loanId)});

            return retVal != -1;
        } catch (Exception ex) {
            Log.e("MemberLoanIssuedRepo.updateMemberLoanBalances", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    public boolean updateMemberLoanBalancesAndComment(int loanId, double balance, Date newDateDue, String comment) {
        SQLiteDatabase db = null;
        boolean performUpdate = false;

        try {
            //TODO: Use a direct query to update the balances on the DB
            db = DatabaseHandler.getInstance(context).getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(LoanIssueSchema.COL_LI_PRINCIPAL_AMOUNT, balance);
            values.put(LoanIssueSchema.COL_LI_BALANCE, balance);
            values.put(LoanIssueSchema.COL_LI_TOTAL_REPAID, (double) 0);
            values.put(LoanIssueSchema.COL_LI_COMMENT, comment);
            //If the loan has been cleared, then the newDateDue will be null
            if (null != newDateDue) {
                values.put(LoanIssueSchema.COL_LI_DATE_DUE, Utils.formatDateToSqlite(newDateDue));
            } else {
                //I use ContentValues.putNull(sKey) but I can just leave this line out
                values.putNull(LoanIssueSchema.COL_LI_DATE_DUE);
            }
            //Determine whether to flag the loan as cleared
            if (balance <= 0) {
                values.put(LoanIssueSchema.COL_LI_IS_CLEARED, 1);
                values.put(LoanIssueSchema.COL_LI_DATE_CLEARED, Utils.formatDateToSqlite(new Date()));
            }

            // Inserting or UpdatingRow
            long retVal = -1;

            // updating row
            retVal = db.update(LoanIssueSchema.getTableName(), values, LoanIssueSchema.COL_LI_LOAN_ID + " = ?",
                    new String[]{String.valueOf(loanId)});

            return retVal != -1;
        } catch (Exception ex) {
            Log.e("MemberLoanIssuedRepo.updateMemberLoanBalances", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }


    // Deleting single entity
    public boolean deleteLoan(int loanId) {
        SQLiteDatabase db = null;
        try {

            db = DatabaseHandler.getInstance(context).getWritableDatabase();

            // To remove all rows and get a count pass "1" as the whereClause.
            db.delete(LoanIssueSchema.getTableName(), LoanIssueSchema.COL_LI_LOAN_ID + " = ?",
                    new String[]{String.valueOf(loanId)});

            return true;
        } catch (Exception ex) {
            Log.e("MeetingLoanIssuedRepo.deleteLoan", ex.getMessage());
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }
}
