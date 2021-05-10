package com.starrysky.nextor;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private EditorFragment editorFragment;
    private ActionBar actionBar;
    private ActionBarDrawerToggle toggle;
    private MenuItem menuItem;
    private int index = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        MenuItem settings = menu.findItem(R.id.settings);
        MenuItem edit = menu.findItem(R.id.edit);
        MenuItem file = menu.findItem(R.id.file);
        if (getSupportFragmentManager().findFragmentByTag("settings") != null || getSupportFragmentManager().findFragmentByTag("file") != null) {
            settings.setVisible(false);
            edit.setVisible(false);
            file.setVisible(false);
            toggle.setDrawerIndicatorEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            if (getSupportFragmentManager().findFragmentByTag("settings") != null) {
                actionBar.setTitle(R.string.settings);
            } else if (getSupportFragmentManager().findFragmentByTag("file") != null) {
                actionBar.setTitle(R.string.file);
            }
        } else {
            settings.setVisible(true);
            edit.setVisible(true);
            file.setVisible(true);
            actionBar.setDisplayHomeAsUpEnabled(false);
            toggle.setDrawerIndicatorEnabled(true);
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            if (editorFragment != null) {
                actionBar.setTitle(editorFragment.getFilename());
                menuItem.setTitle(editorFragment.getFilename());
            } else {
                actionBar.setTitle("");
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        } else if (itemId == R.id.settings) {
            startFragment(R.id.container_layout, new SettingsFragment(), "settings");
            invalidateOptionsMenu();
        } else if (itemId == R.id.new_file) {
            newFile();
        } else if (itemId == R.id.save) {
            if (editorFragment != null) {
                if (editorFragment.getFile() != null) {
                    save();
                } else {
                    saveAs("null");
                }
            }
        } else if (itemId == R.id.save_as) {
            saveAs("null");
        } else if (itemId == R.id.close) {
            closeDialog();
        } else if (itemId == R.id.open_file) {
            openFile();
        } else if (itemId == R.id.undo) {
            if (editorFragment != null) {
                editorFragment.undo();
            }
        } else if (itemId == R.id.redo) {
            if (editorFragment != null) {
                editorFragment.redo();
            }
        } else if (itemId == R.id.select_all) {
            if (editorFragment != null) {
                editorFragment.selectAll();
            }
        } else if (itemId == R.id.copy) {
            if (editorFragment != null) {
                editorFragment.copyAndCut("copy");
            }
        } else if (itemId == R.id.cut) {
            if (editorFragment != null) {
                editorFragment.copyAndCut("cut");
            }
        } else if (itemId == R.id.paste) {
            if (editorFragment != null) {
                editorFragment.paste();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        invalidateOptionsMenu();
    }

    private void init() {
        immersion();
        permission();
        initView();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        navigationView.setItemIconTintList(null);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        toggle.syncState();
        drawerLayout.addDrawerListener(toggle);
        toggle.setToolbarNavigationClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FileFragment fileFragment = (FileFragment) getSupportFragmentManager().findFragmentByTag("file");
                if (fileFragment == null) {
                    if (!toggle.isDrawerIndicatorEnabled()) {
                        onBackPressed();
                    }
                } else {
                    fileFragment.backLast();
                }
            }
        });
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("");
        }
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                item.setCheckable(true);
                item.setChecked(true);
                menuItem = item;
                drawerLayout.closeDrawers();
                for (int i = 0; i < index; i++) {
                    if (getSupportFragmentManager().findFragmentByTag(String.valueOf(i)) != null) {
                        EditorFragment fragment = (EditorFragment) getSupportFragmentManager().findFragmentByTag(String.valueOf(i));
                        if (fragment != null) {
                            if (i == item.getItemId()) {
                                getSupportFragmentManager().beginTransaction().show(fragment).commit();
                                editorFragment = fragment;
                                actionBar.setTitle(editorFragment.getFilename());
                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(editorFragment.getEditText().getWindowToken(), 0);
                            } else {
                                getSupportFragmentManager().beginTransaction().hide(fragment).commit();
                            }
                        }
                    }
                }
                return true;
            }
        });
    }

    private void immersion() {
        View view = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT >= 21) {
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            }
        }
    }

    private void permission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        1);
            }
        }
    }

    public void startFragment(int id, Fragment fragment, String tag) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(id, fragment, tag);
        List<Fragment> list = fragmentManager.getFragments();
        for (int j = 0; j < list.size(); j++) {
            fragmentTransaction.hide(list.get(j));
        }
        if (fragment.getTag() != null && (fragment.getTag().equals("settings") || fragment.getTag().equals("file"))) {
            fragmentTransaction.addToBackStack(null);
        }
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
        try {
            editorFragment = (EditorFragment) fragment;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void newFile() {
        menuItem = navigationView.getMenu().add(1, index, 0, R.string.untitled).setIcon(R.drawable.my_file);
        EditorFragment editorFragment = new EditorFragment();
        editorFragment.setFilename(getResources().getString(R.string.untitled));
        startFragment(R.id.container_layout, editorFragment, String.valueOf(index));
        navigationView.getMenu().findItem(index).setCheckable(true).setChecked(true);
        actionBar.setTitle(navigationView.getMenu().findItem(index).getTitle());
        index++;
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editorFragment.getEditText().getWindowToken(), 0);
    }

    public void save() {
        if (editorFragment != null) {
            editorFragment.write();
            actionBar.setTitle(editorFragment.getFilename());
        }
    }

    public void saveAs(String fun) {
        if (editorFragment != null) {
            FileFragment fileFragment = new FileFragment();
            Bundle bundle = new Bundle();
            bundle.putString("fun", fun);
            fileFragment.setArguments(bundle);
            fileFragment.setEditorFragment(editorFragment);
            fileFragment.setOperation(false);
            startFragment(R.id.container_layout, fileFragment, "file");
            invalidateOptionsMenu();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editorFragment.getEditText().getWindowToken(), 0);
        }
    }

    public void saveAs(File file) {
        String name = file.getName();
        MenuItem item = null;
        if (editorFragment.getTag() != null) {
            item = navigationView.getMenu().findItem(Integer.parseInt(editorFragment.getTag()));
        }
        if (item != null) {
            item.setTitle(name);
        }
        actionBar.setTitle(name);
    }

    public void openFile() {
        if (editorFragment != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editorFragment.getEditText().getWindowToken(), 0);
        }
        FileFragment fileFragment = new FileFragment();
        startFragment(R.id.container_layout, fileFragment, "file");
        invalidateOptionsMenu();
    }

    public void openFile(File file) {
        boolean flag = true;
        List<Fragment> list = getSupportFragmentManager().getFragments();
        for (int i = 0; i < list.size(); i++) {
            try {
                EditorFragment fragment = (EditorFragment) list.get(i);
                if (file.getPath().equals(fragment.getFile().getPath())) {
                    Toast.makeText(this, R.string.file_opened, Toast.LENGTH_SHORT).show();
                    flag = false;
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (flag) {
            String name = file.getName();
            MenuItem item = navigationView.getMenu().add(1, index, 0, name);
            item.setIcon(R.drawable.my_file);
            startFragment(R.id.container_layout, new EditorFragment(), String.valueOf(index));
            menuItem = navigationView.getMenu().findItem(index).setCheckable(true).setChecked(true);
            index++;
            editorFragment.setFile(file);
            editorFragment.setFilename(name);
            editorFragment.read();
        }
    }

    public void close() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editorFragment.getEditText().getWindowToken(), 0);
        if (editorFragment != null) {
            int num = 0;
            if (editorFragment.getTag() != null) {
                num = Integer.parseInt(editorFragment.getTag());
            }
            getSupportFragmentManager().beginTransaction().remove(editorFragment).commit();
            navigationView.getMenu().removeItem(num);
            List<Fragment> list = new ArrayList<>();
            for (int i = 0; i < getSupportFragmentManager().getFragments().size(); i++) {
                String tag = getSupportFragmentManager().getFragments().get(i).getTag();
                if (isNumeric(tag) && !tag.equals(String.valueOf(num))) {
                    list.add(getSupportFragmentManager().getFragments().get(i));
                }
            }
            if (list.size() >= 1) {
                num = 0;
                for (int i = 0; i < list.size(); i++) {
                    if (num < Integer.parseInt(list.get(i).getTag())) {
                        num = Integer.parseInt(list.get(i).getTag());
                    }
                }
                editorFragment = (EditorFragment) getSupportFragmentManager().findFragmentByTag(String.valueOf(num));
                getSupportFragmentManager().beginTransaction().show(editorFragment).commit();
                navigationView.getMenu().findItem(num).setCheckable(true).setChecked(true);
                actionBar.setTitle(navigationView.getMenu().findItem(num).getTitle());
            } else {
                editorFragment = null;
                actionBar.setTitle("");
            }
        }
    }

    public void setName(String str) {
        actionBar.setTitle(str);
        menuItem.setTitle(str);
    }

    public static boolean isNumeric(String str) {
        if (str == null) return false;
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) return false;
        }
        return true;
    }


    public void closeDialog() {
        if (editorFragment != null) {
            if (editorFragment.getFilename().charAt(0) == '*') {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.warning);
                builder.setMessage(R.string.is_save);
                builder.setCancelable(true);
                builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editorFragment.getFile() != null) {
                            save();
                        } else {
                            saveAs("close");
                        }
                        if (editorFragment.getFile() != null) {
                            close();
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        close();
                        dialog.dismiss();
                    }
                });
                builder.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            } else {
                close();
            }
        }
    }
}
