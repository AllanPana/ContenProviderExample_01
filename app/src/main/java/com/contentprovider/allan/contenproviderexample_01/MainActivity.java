package com.contentprovider.allan.contenproviderexample_01;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private EditText et_Name, et_bDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_Name = (EditText) findViewById(R.id.name);
        et_bDay = (EditText) findViewById(R.id.birthday);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addBirthday(View view) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MyContentProvider.NAME, String.valueOf(et_Name.getText()));
        contentValues.put(MyContentProvider.BIRTHDAY, String.valueOf(et_bDay.getText()));

        if(contentValues != null){
            Uri uri = getContentResolver().insert(MyContentProvider.CONTENT_URI,contentValues);
            Toast.makeText(getBaseContext(),uri + "\ninserted successfully!", Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getBaseContext(),"Please enter data to save into content provider", Toast.LENGTH_LONG).show();
        }

    }

    public void showAllBirthdays(View view) {
        Uri friendsUri = Uri.parse(MyContentProvider.PROVIDER_URL);
        Log.d("url provider",MyContentProvider.PROVIDER_URL);
        Cursor cursor = getContentResolver().query(friendsUri, null, null, null, "name");

        String name = "";
        String bDay = "";
        String id = "";

        String data = "";
        if(!cursor.moveToFirst()){
            Toast.makeText(getBaseContext(),"Content Provider is empty", Toast.LENGTH_LONG).show();
        }else{
            do{
                name = cursor.getString(cursor.getColumnIndex(MyContentProvider.NAME));
                bDay = cursor.getString(cursor.getColumnIndex(MyContentProvider.BIRTHDAY));
                id = cursor.getString(cursor.getColumnIndex(MyContentProvider.ID));

                data +=  "\n"+id + " : " + name + " : " + bDay;

            }while (cursor.moveToNext());
            Toast.makeText(getBaseContext(),data, Toast.LENGTH_LONG).show();
        }
    }

    public void deleteAllBirthdays(View view) {
        Uri friendsUri = Uri.parse(MyContentProvider.PROVIDER_URL);
        int count = getContentResolver().delete(friendsUri,null,null);
        Toast.makeText(getBaseContext(),count + "\nrecords deleted successfully!", Toast.LENGTH_LONG).show();
    }
}
