package de.uwr1.training;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Created by f00f on 12.07.2014.
 */
public class TrainingData {
    public String Wochentag;
    public String Date;
    public String Time;
    public String Location;
    public String Zusagen = "---";
    public String Absagen = "---";
    public String Nixsagen = "---";
    public long Expires; // wann ist das Training (zu Ende)?
    public long Updated; // von wann ist der letzte Eintrag?
    public long Timestamp; // wann wurde das JSON runtergeladen?
    public String Temp;
    public long TempUpdated;
    public String PhotoURL;
    public String PhotoThumbURL;
    // private
    private String[] ZusagenArr;
    private String[] AbsagenArr;
    public String[] NixsagenArr;
    private String[] ZusagenNamesArr;
    private String[] AbsagenNamesArr;

    // PUBLIC METHODS

    // compose string with meta information about the training
    public String getGeneralInfo() {
        String info = Date + " um " + Time + " in " + Location;
        if (null != Wochentag)
            info = Wochentag + ", " + info;
        return info;
    }

    public int getNumZusagen() {
        return ZusagenArr.length;
    }
    public int getNumAbsagen() {
        return AbsagenArr.length;
    }
    public int getNumNixsager() {
        return NixsagenArr.length;
    }

    // check whether a user will not participate
    public boolean hatAbgesagt(String username) {
        return containsName(AbsagenNamesArr, username);
    }
    // check whether a user will participate
    public boolean hatZugesagt(String username) {
        return containsName(ZusagenNamesArr, username);
    }

    // check if the training data is for a session in the past
    public boolean isExpired() {
        long now = new Date().getTime();
        return now > (this.Expires * 1000);
    }

    // Load data from several JSON strings
    public boolean parseJSON(String json) {
        Zusagen = "---";
        Absagen = "---";
        Nixsagen = "---";

        JSONObject jo;
        try {
            jo = new JSONObject(json).getJSONObject("train");
        } catch(JSONException e) {
            Log.w("UWR_Training::Training::parseJSON", "Unable to parse JSON data.");
            return false;
        }
        // get basic information
        try {
            Expires = jo.getLong("end");
            Wochentag = jo.getString("wtag");
            Date = jo.getString("datum");
            Time = jo.getString("zeit");
            Location = jo.getString("ort");
            Updated = jo.getLong("updated");
        } catch(JSONException e) {
            Log.w("UWR_Training::Training::parseJSON", "Unable to parse JSON data.");
            return false;
        }
        // get optional information
        try {
            ZusagenArr = getStringArray(jo, "zu");
        } catch(JSONException e) { /* empty */ }
        try {
            AbsagenArr = getStringArray(jo, "ab");
        } catch(JSONException e) { /* empty */ }
        try {
            NixsagenArr = getStringArray(jo, "nix");
        } catch(JSONException e) { /* empty */ }
        // get optional information
        JSONObject joX = null;
        try {
            joX = jo.getJSONObject("x");
        } catch(JSONException e) { /* empty */ }
        if (null != joX) {
            try {
                JSONObject joTemp = joX.getJSONObject("temp");
                Temp = joTemp.getString("deg");
                TempUpdated = joTemp.getLong("updated");
            } catch(JSONException e) { /* empty */ }
            try {
                JSONObject joPic = joX.getJSONObject("pic");
                PhotoURL = joPic.getString("full");
                PhotoThumbURL = joPic.getString("thumb");
            } catch(JSONException e) { /* empty */ }
        }

        Timestamp = new java.util.Date().getTime();

        if (null == ZusagenArr)
            ZusagenArr = new String[0];
        if (null == AbsagenArr)
            AbsagenArr = new String[0];
        if (ZusagenArr.length > 0) {
            Zusagen = android.text.TextUtils.join(", ", ZusagenArr);
            ZusagenNamesArr = new String[ZusagenArr.length];
            for (int i = 0; i < ZusagenArr.length; i++){
                ZusagenNamesArr[i] = getName(ZusagenArr[i]);
            }
        }
        if (AbsagenArr.length > 0) {
            Absagen = android.text.TextUtils.join(", ", AbsagenArr);
            AbsagenNamesArr = new String[AbsagenArr.length];
            for (int i = 0; i < AbsagenArr.length; i++){
                AbsagenNamesArr[i] = getName(AbsagenArr[i]);
            }
        }

        // TODO: load json data
        json = "{\"names\":[\"Lorem\",\"Ipsum\",\"Dolor\"]}";
        String[] allPlayers = null;
        try {
            allPlayers = getStringArray(new JSONObject(json), "names");
        } catch (JSONException e) { /* empty */ }
        if (null == allPlayers || 0 == allPlayers.length) {
            NixsagenArr = new String[] {};
        } else {
            ArrayList<String> NixsagerList = new ArrayList<String>();
            for (String player : allPlayers) {
                if (hatZugesagt(player))
                    continue;
                if (hatAbgesagt(player))
                    continue;
                NixsagerList.add(player);
            }
            NixsagenArr = NixsagerList.toArray(new String[]{});
            Arrays.sort(NixsagenArr);
        }

        if (null == NixsagenArr)
            NixsagenArr = new String[0];

        if (NixsagenArr.length > 0)
            Nixsagen = android.text.TextUtils.join(", ", NixsagenArr);

        return true;
    }

    // PRIVATE METHODS

    private static boolean containsName(String[] _sagenNamesArr, String username) {
        if (null == _sagenNamesArr || null == username || username.isEmpty())
            return false;

        for (String text : _sagenNamesArr) {
            if (!text.startsWith(username)) {
                continue;
            }
            if (text.equalsIgnoreCase(username)) {
                return true;
            }
            if (text.length() <= username.length()) {
                continue;
            }
            char c = text.charAt(username.length());
            boolean isLowercaseLetter = (c >= 'a' && c <= 'z');
            boolean isUppercaseLetter = (c >= 'A' && c <= 'Z');
            boolean isUmlaut = false; // BUG HERE: yes, we ignore Umlaute
            if (!isLowercaseLetter && !isUppercaseLetter && !isUmlaut) {
                return true;
            }
        }
        return false;
    }

    // TODO: implement FirstWord
    private static String getName(String s) {
        return s;
    }

    // Extract String array of 'name' out of the JSON object 'jo'.
    private static String[] getStringArray(JSONObject jo, String name) throws JSONException {
        JSONArray ja = jo.getJSONArray(name);
        if (0 == ja.length()) {
            return null;
        }

        String[] res = new String[ja.length()];
        for (int i = 0; i < ja.length(); i++) {
            res[i] = ja.getString(i);
        }

        if (1 == res.length && res[0].isEmpty()) {
            return null;
        }

        return res;
    }

}
