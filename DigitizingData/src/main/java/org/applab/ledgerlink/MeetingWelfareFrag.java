package org.applab.ledgerlink;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragment;

import org.applab.ledgerlink.domain.model.Member;
import org.applab.ledgerlink.fontutils.RobotoTextStyleExtractor;
import org.applab.ledgerlink.fontutils.TypefaceManager;
import org.applab.ledgerlink.helpers.LongTaskRunner;
import org.applab.ledgerlink.helpers.MembersWelfareArrayAdapter;
import org.applab.ledgerlink.helpers.Utils;

import java.util.ArrayList;

public class MeetingWelfareFrag extends SherlockFragment {

    ActionBar actionBar;
    ArrayList<Member> members;
    String meetingDate;
    int meetingId;
    private MeetingActivity parentActivity;
    private RelativeLayout fragmentView;
    private MembersWelfareArrayAdapter adapter;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentActivity = (MeetingActivity) getSherlockActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (container == null) {
            // We have different layouts, and in one of them this
            // fragment's containing frame doesn't exist.  The fragment
            // may still be created from its saved state, but there is
            // no reason to try to create its view hierarchy because it
            // won't be displayed.  Note this is not needed -- we could
            // just run the code below, where we would create and return
            // the view hierarchy; it would just never be used.
            return null;
        }
        fragmentView =  (RelativeLayout)inflater.inflate(R.layout.fragment_meeting_welfare, container, false);
        initializeFragment();
        return fragmentView;
    }

    private void initializeFragment()
    {

        TypefaceManager.addTextStyleExtractor(RobotoTextStyleExtractor.getInstance());
        actionBar = getSherlockActivity().getSupportActionBar();
        meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
        String title = "Meeting";
        switch(Utils._meetingDataViewMode) {
            case VIEW_MODE_REVIEW:
                title = "Send Data";
                break;
            case VIEW_MODE_READ_ONLY:
                title = "Sent Data";
                break;
            default:
                //title="Meeting";
                break;
        }
        actionBar.setTitle(title);
        actionBar.setSubtitle(meetingDate);
        /**TextView lblMeetingDate = (TextView)getSherlockActivity().findViewById(R.id.lblMSavFMeetingDate);
         meetingDate = getSherlockActivity().getIntent().getStringExtra("_meetingDate");
         lblMeetingDate.setText(meetingDate); */
        meetingId = getSherlockActivity().getIntent().getIntExtra("_meetingId", 0);
        //Wrap long task in runnable an run asynchronously
        Runnable populateRunnable = new Runnable()
        {
            @Override
            public void run()
            {
                //Populate the Members
                populateMembersList();
            }
        };
        LongTaskRunner.runLongTask(populateRunnable, "Please wait", "Loading welfare information", parentActivity);
    }

    //Populate Members List
    private void populateMembersList() {
        //Load the Main Menu
        members = parentActivity.ledgerLinkApplication.getMemberRepo().getAllMembers();

        //Now get the data via the adapter
        adapter = new MembersWelfareArrayAdapter(getSherlockActivity().getBaseContext(), members);
        adapter.setMeetingId(meetingId);

        //Assign Adapter to ListView
        //OMM: Since I was unable to do a SherlockListFragment to work
        //setListAdapter(adapter);
        final ListView lvwMembers = (ListView)fragmentView.findViewById(R.id.lvwMSavFMembers);
        final TextView txtEmpty = (TextView)fragmentView.findViewById(R.id.txtMSavFEmpty);

        parentActivity.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {

                lvwMembers.setEmptyView(txtEmpty);
                lvwMembers.setAdapter(adapter);
            }
        });


        // listening to single list item on click
        lvwMembers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Do not invoke the event when in Read only Mode
                if(parentActivity.isViewOnly()) {
                    Toast.makeText(getSherlockActivity().getApplicationContext(), R.string.meeting_is_readonly_warning, Toast.LENGTH_LONG).show();
                    return;
                }
                if(Utils._meetingDataViewMode != Utils.MeetingDataViewMode.VIEW_MODE_READ_ONLY) {
                    Member selectedMember = (Member) members.get(position);
                    Intent i = new Intent(view.getContext(), MemberWelfareHistoryActivity.class);

                    // Pass on data
                    i.putExtra("_meetingDate",meetingDate);
                    i.putExtra("_memberId", selectedMember.getMemberId());
                    i.putExtra("_names", selectedMember.toString());
                    i.putExtra("_meetingId",meetingId);

                    startActivity(i);
                }
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        this.populateMembersList();
    }
}