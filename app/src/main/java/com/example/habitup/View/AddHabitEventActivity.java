package com.example.habitup.View;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.habitup.Controller.ElasticSearchController;
import com.example.habitup.Controller.HabitUpController;
import com.example.habitup.Model.Habit;
import com.example.habitup.Model.HabitEvent;
import com.example.habitup.R;

import java.text.DateFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AddHabitEventActivity extends AppCompatActivity {

    // Event completion date
    private TextView dateView;
    private int year_x, month_x, day_x;
    private static final int DIALOG_ID = 1;

    // Clickable image
    private static final int REQUEST_CODE = 1;
    private Bitmap imageBitMap;
    private Button imageButton;
    private ImageView image;

    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_event);

        // Set back button
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Get current date
        final Calendar cal = Calendar.getInstance(Locale.CANADA);
        year_x = cal.get(Calendar.YEAR);
        month_x = cal.get(Calendar.MONTH);
        day_x = cal.get(Calendar.DAY_OF_MONTH);

        // Get date click button
        ImageView dateClicker = (ImageView) findViewById(R.id.event_date_button);
        dateView = (TextView) findViewById(R.id.event_date_text);

        // Set selected date
        setDateString();

        // Set up spinner
        Spinner habitSpinner = (Spinner) findViewById(R.id.event_habit_spinner);

        // Set up habit types list
        ArrayList<String> habitNames = new ArrayList<>();

        // TODO: Retrieve habits from current user's HabitList
        ArrayList<Habit> habitList = new ArrayList<Habit>();
        for (Habit habit : habitList) {
            habitNames.add(habit.getHabitName());
        }
        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_spinner_item, habitNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        habitSpinner.setAdapter(adapter);

        // Get location checkbox
        Switch locationSwitch = (Switch) findViewById(R.id.location_switch);

        // Get photo icon
        imageButton = (Button) findViewById(R.id.photo_icon);
        image = (ImageView) findViewById(R.id.taken_image);

        // Allow user to take or choose photo when clicking the photo icon
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (photoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(photoIntent, REQUEST_CODE);
                }
            }
        });

        // Open the date picker dialog when clicking calendar button
        dateClicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });

        // Open the date picke dialog when clicking date field
        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(DIALOG_ID);
            }
        });

        // Save button
        saveButton = (Button) findViewById(R.id.save_event);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                String habitType = ((Spinner) findViewById(R.id.event_habit_spinner)).getSelectedItem().toString();
                String habitComment = ((TextView) findViewById(R.id.event_comment)).getText().toString();

                String completeDateString = ((TextView) findViewById(R.id.event_date_text)).getText().toString();
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
                LocalDate completeDate = LocalDate.parse(completeDateString, formatter);

                Log.i("DATE:", completeDate.toString()); // TODO REMOVE

                // TODO: M5 get location here
                Bitmap photo = ((BitmapDrawable) ((ImageView) findViewById(R.id.taken_image)).getDrawable()).getBitmap();

                int uid = HabitUpController.getCurrentUID();
                int hid = 0; // TODO DEBUG REMOVE
//                int hid = ElasticSearchController (look up habit to get hid);

                HabitEvent newEvent = new HabitEvent(uid, hid);
                Boolean eventOK = Boolean.TRUE;

                newEvent.setHabit(hid);  // TODO: get actual HID from habitType

                try {
                    newEvent.setComment(habitComment);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    eventOK = Boolean.FALSE;
                }

                try {
                    newEvent.setCompletedate(completeDate);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    eventOK = Boolean.FALSE;
                }

                try {
                    newEvent.setImage(photo);
                } catch (IllegalArgumentException e) {
                    Toast.makeText(getBaseContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    eventOK = Boolean.FALSE;
                }

                newEvent.setScheduled();

                if (eventOK) {
                    // Pass to the controller
                    HabitUpController hupCtl = new HabitUpController();

                    if (hupCtl.addHabitEvent(newEvent) == 0) {
                        Intent result = new Intent();
                        setResult(Activity.RESULT_OK, result);
                        finish();
                    } else {
                        Toast.makeText(getBaseContext(), "There was an error adding the new HabitEvent.", Toast.LENGTH_LONG).show();
                    }
                }

            }

        });

        viewMode();
    }

    /**
     * Opening the date picker dialog
     * @param id the dialog id
     * @return the dialog
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == DIALOG_ID) {
            return new DatePickerDialog(this, datePicker, year_x, month_x, day_x);
        }
        return null;
    }

    /**
     * Includes activity for taking picture
     * @param requestCode the request code for some activity
     * @param resultCode the result code of the activity
     * @param data data from the activity
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitMap = (Bitmap) extras.get("data");

            // TODO: Resize image for to appropriate byte size
            image.setImageBitmap(imageBitMap);
            image.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Listens for when the user clicks on the back button
     * @param menuItem the item in the menu
     * @return true if the item was selected
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    /**
     * Updates the date string in the date text view
     */
    private void setDateString() {
        String monthName = new DateFormatSymbols().getShortMonths()[month_x];
        String dateString = (monthName) + " " + day_x + ", " + year_x;
        dateView.setText(dateString);
    }

    // Creating the date picker listener for when a user selects a date
    private DatePickerDialog.OnDateSetListener datePicker
            = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            year_x = year;
            month_x = monthOfYear;
            day_x = dayOfMonth;
            setDateString();
        }
    };

    // DELETE FOR LATER AND MOVE TO EditHabitEventActivity
    private void viewMode() {
        // Disable habit type spinner
        Spinner spinner = (Spinner) findViewById(R.id.event_habit_spinner);
        spinner.setBackgroundResource(0);
        spinner.setBackgroundColor(getResources().getColor(R.color.white));
        spinner.setPadding(0, 0, 0, 0);
        spinner.setEnabled(false);

        // Disable date clickables
        ImageView dateButton = (ImageView) findViewById(R.id.event_date_button);
        TextView dateText = (TextView) findViewById(R.id.event_date_text);
        dateButton.setVisibility(View.INVISIBLE);
        dateButton.setOnClickListener(null);
        dateText.setBackgroundResource(0);
        dateText.setPadding(0, 0, 0, 0);
        dateText.setOnClickListener(null);

        // Disable location switch
        Switch locSwitch = (Switch) findViewById(R.id.location_switch);
        locSwitch.setClickable(false);
        locSwitch.setBackgroundResource(0);
        TextView markerLabel = (TextView) findViewById(R.id.marker_label);
        markerLabel.setBackgroundResource(0);

        // Disable comment field
        EditText editComment = (EditText) findViewById(R.id.event_comment);
        editComment.setBackgroundResource(0);
        editComment.setPadding(0, 0, 0, 0);
        editComment.setFocusable(false);

        // Disable photo button
        // TODO: Check whether event has an image, if it does not, remove all photo labels
        RelativeLayout photoLayout = (RelativeLayout) findViewById(R.id.photo_display);
        photoLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        Button photoButton = (Button) findViewById(R.id.photo_icon);
        photoButton.setVisibility(View.INVISIBLE);

        // Disable save button
        Button saveButton = (Button) findViewById(R.id.save_event);
        saveButton.setVisibility(View.INVISIBLE);
    }
}
