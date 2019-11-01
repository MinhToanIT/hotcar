package com.app.hotgo.activities;

import android.os.Bundle;
import android.widget.TextView;

import com.app.hotgo.BaseActivity;
import com.app.hotgo.R;
import com.app.hotgo.config.GlobalValue;
import com.app.hotgo.object.ItemTripHistory;

import java.util.ArrayList;

public class DetailTripHistoryActivity extends BaseActivity {

	TextView tripID;
	TextView nameDriver;
	TextView timeStart;
	TextView timeEnd;
	TextView desparture;
	TextView destination;
	TextView totalTime;
	TextView totalDistance;
	TextView totalPoint;
	TextView linkTrip;
	
	ArrayList<ItemTripHistory> listHistory;
	private ItemTripHistory itemHistory;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail_history);
		listHistory = new ArrayList<ItemTripHistory>();
		
		initUI();
		initControl();
		getDataFromGlobal();
		getData();
		initAndSetHeader(R.string.lbl_trip_history);
	}

	private void initControl() {
		
		tripID = findViewById(R.id.txtTripID);
		linkTrip = findViewById(R.id.txtLinkTrip);
		timeEnd = findViewById(R.id.txtTimeEnd);
		desparture = findViewById(R.id.txtDesparture);
		destination = findViewById(R.id.txtDestination);
		totalTime = findViewById(R.id.txtTotalTime);
		totalDistance = findViewById(R.id.txtTotalDistance);
		totalPoint = findViewById(R.id.txtTotalPoint);
	}
	private void getDataFromGlobal() {
		itemHistory = GlobalValue.getInstance().currentHistory;
	}
	
	public void getData(){
		tripID.setText(String.valueOf(itemHistory.getTripId()));
		linkTrip.setText(itemHistory.getLink());
		timeEnd.setText(itemHistory.getEndTime());
		desparture.setText(itemHistory.getStartLocaton());
		destination.setText(itemHistory.getEndLocation());
		totalTime.setText(itemHistory.getTotalTime()+"\t"+"Min");
		totalDistance.setText(itemHistory.getDistance().toString()+"\t"+"Km");
		totalPoint.setText(itemHistory.getActualFare().toString());
	}
}
