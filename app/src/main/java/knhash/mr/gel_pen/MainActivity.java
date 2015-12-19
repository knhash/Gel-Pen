package knhash.mr.gel_pen;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int SHARE_ID = Menu.FIRST;
    private static final int DELETE_ID = Menu.FIRST + 2;
    private static final int EDIT_ID = Menu.FIRST + 1;

    private GelAdapter mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_create);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                createNote();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //
        mDbHelper = new GelAdapter(this);
        mDbHelper.open();
        final ListView listview = (ListView) findViewById(R.id.list);
        fillData(listview);
        registerForContextMenu(listview);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Intent i = new Intent(MainActivity.this, GelEdit.class);
                i.putExtra(GelAdapter.KEY_ROWID, id);
                startActivityForResult(i, ACTIVITY_EDIT);

                /*//View v = (View)view.getParent();
                TextView txt2 = (TextView) view.findViewById(R.id.text2);
                String s = txt2.getText().toString();
                int count = Integer.parseInt(s);
                count++;
                txt2.setText(String.valueOf(count));

                txt2.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);*/
            }
        });
    }

    private void fillData(ListView listview) {
        // Get all of the rows from the database and create the item list
        Cursor mNotesCursor = mDbHelper.fetchAllNotes();
        startManagingCursor(mNotesCursor);

        // Create an array to specify the fields we want to display in the list (only TITLE)
        String[] from = new String[]{GelAdapter.KEY_TITLE,GelAdapter.KEY_BODY,GelAdapter.KEY_DATE};

        // and an array of the fields we want to bind those fields to)
        int[] to = new int[]{R.id.text1,R.id.text2,R.id.text3};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter notes =
                new SimpleCursorAdapter(this, R.layout.notes_row, mNotesCursor, from, to);
        listview.setAdapter(notes);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(3, SHARE_ID, 3, R.string.menu_share);
        menu.add(2, DELETE_ID, 2, R.string.menu_delete);
        menu.add(1, EDIT_ID, 1, R.string.menu_edit);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {

        switch(item.getItemId()) {

            case DELETE_ID:
                AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteNote(info.id);
                ListView listview = (ListView) findViewById(R.id.list);
                fillData(listview);
                return true;
            case EDIT_ID:
                info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                Intent i = new Intent(MainActivity.this, GelEdit.class);
                i.putExtra(GelAdapter.KEY_ROWID, info.id);
                startActivityForResult(i, ACTIVITY_EDIT);
                return true;
            case SHARE_ID:
                /*To-do sharing note*/
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createNote() {
        Intent i = new Intent(this, GelEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        ListView listview = (ListView) findViewById(R.id.list);
        fillData(listview);
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

        if (id == R.id.INSERT_QUICK) {
            //createNote();
            new MaterialDialog.Builder(this)
                    .title("Quicknote")
                    .content("Add a new note")
                    .contentGravity(GravityEnum.CENTER)
                    .titleGravity(GravityEnum.CENTER)
                    .inputType(InputType.TYPE_CLASS_TEXT)
                    .input("", "", new MaterialDialog.InputCallback() {
                        @Override
                        public void onInput(MaterialDialog dialog, CharSequence input) {
                            mDbHelper.open();
                            String title = input.toString();
                            if (title.equals("")) {
                                Toast.makeText(MainActivity.this, "Empty note discarded", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            mDbHelper.createNote(title, "", "");
                            ListView listview = (ListView) findViewById(R.id.list);
                            fillData(listview);
                        }
                    })
                    .show();
            return true;
        }

        /*if (id == R.id.INSERT_NOTE) {
            createNote();
            return true;
        }*/

        /*if (id == R.id.ABOUT) {
            new MaterialDialog.Builder(this)
                    .title(R.string.about_title)
                    .content(R.string.about_content)
                    .titleGravity(GravityEnum.CENTER)
                    .contentGravity(GravityEnum.CENTER)
                    .icon(getResources().getDrawable(R.mipmap.ic_launcher))
                    .show();
        }*/
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_backup) {
            exportDB();

        } else if (id == R.id.nav_restore) {
            importDB();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private void exportDB(){
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source;//=null;
        FileChannel destination;//=null;
        String currentDBPath = "/data/"+ "knhash.mr.gel_pen" +"/databases/"+"data";
        String backupDBPath = "/data/"+ "knhash.mr.gel_pen" +"/databases/"+"back-up";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        if (!backupDB.exists()) {
            if (!backupDB.getParentFile().mkdirs()) {
                Log.e("File Create Error", "Problem creating Backup file");
            }
        }
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Toast.makeText(this, "Notes Backed-up!", Toast.LENGTH_SHORT).show();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private void importDB(){
        File sd = Environment.getExternalStorageDirectory();
        File data = Environment.getDataDirectory();
        FileChannel source;//=null;
        FileChannel destination;//=null;
        String currentDBPath = "/data/"+ "knhash.mr.gel_pen" +"/databases/"+"data";
        String backupDBPath = "/data/"+ "knhash.mr.gel_pen" +"/databases/"+"back-up";
        File currentDB = new File(data, currentDBPath);
        File backupDB = new File(sd, backupDBPath);
        if (!backupDB.exists()) {
            Toast.makeText(this, "Nothing to restore", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            source = new FileInputStream(backupDB).getChannel();
            destination = new FileOutputStream(currentDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            ListView listview = (ListView) findViewById(R.id.list);
            fillData(listview);
            Toast.makeText(this, "DB Restored!", Toast.LENGTH_SHORT).show();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
}
