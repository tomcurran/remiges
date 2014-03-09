package org.tomcurran.remiges.liberation;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.database.Cursor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.tomcurran.remiges.liberation.model.Jump;
import org.tomcurran.remiges.liberation.model.JumpType;
import org.tomcurran.remiges.liberation.model.LiberationModel;
import org.tomcurran.remiges.liberation.model.Place;
import org.tomcurran.remiges.provider.RemigesContract.JumpTypes;
import org.tomcurran.remiges.provider.RemigesContract.Jumps;
import org.tomcurran.remiges.provider.RemigesContract.Places;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

import static org.tomcurran.remiges.util.LogUtils.makeLogTag;


public class RemigesLiberation {
    private static final String TAG = makeLogTag(RemigesLiberation.class);

    public static ArrayList<ContentProviderOperation> getDeleteOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newDelete(Jumps.CONTENT_URI).build());
        operations.add(ContentProviderOperation.newDelete(Places.CONTENT_URI).build());
        operations.add(ContentProviderOperation.newDelete(JumpTypes.CONTENT_URI).build());
        return operations;
    }

    public static ArrayList<ContentProviderOperation> getImportOperations(String json) throws ParseException {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        LiberationModel model = new Gson().fromJson(json, LiberationModel.class);

        if (model == null) {
            return operations;
        }

        // Places
        HashMap<Integer, Integer> placesBackRefs = new LinkedHashMap<Integer, Integer>();
        for (Place place : model.places) {
            operations.add(ContentProviderOperation
                    .newInsert(Places.CONTENT_URI)
                    .withValue(Places.PLACE_NAME, place.name)
                    .withValue(Places.PLACE_LONGITUDE, place.longitude)
                    .withValue(Places.PLACE_LATITUDE, place.latitude)
                    .build());
            placesBackRefs.put(place.id, operations.size() - 1);
        }

        // JumpTypes
        HashMap<Integer, Integer> jumpTypeBackRefs = new LinkedHashMap<Integer, Integer>();
        for (JumpType jumpType : model.jumpTypes) {
            operations.add(ContentProviderOperation
                    .newInsert(JumpTypes.CONTENT_URI)
                    .withValue(JumpTypes.JUMPTPYE_NAME, jumpType.name)
                    .build());
            jumpTypeBackRefs.put(jumpType.id, operations.size() - 1);
        }

        // Jumps
        for (Jump jump : model.jumps) {
            operations.add(ContentProviderOperation
                    .newInsert(Jumps.CONTENT_URI)
                    .withValue(Jumps.JUMP_NUMBER, jump.number)
                    .withValue(Jumps.JUMP_DATE, jump.date)
                    .withValue(Jumps.JUMP_DESCRIPTION, jump.description)
                    .withValue(Jumps.JUMP_WAY, jump.way)
                    .withValue(Jumps.JUMP_EXIT_ALTITUDE, jump.exitAltitude)
                    .withValue(Jumps.JUMP_DEPLOYMENT_ALTITUDE, jump.deploymentAltitude)
                    .withValue(Jumps.JUMP_DELAY, jump.delay)
                    .withValueBackReference(Jumps.JUMPTYPE_ID, jumpTypeBackRefs.get(jump.jumpType))
                    .withValueBackReference(Jumps.PLACE_ID, placesBackRefs.get(jump.place))
                    .build());
        }

        return operations;
    }

    public static String getExportJson(ContentResolver resolver) {
        Cursor jumps = resolver.query(Jumps.CONTENT_URI, null, null, null, null);
        Cursor jumpTypes = resolver.query(JumpTypes.CONTENT_URI, null, null, null, null);
        Cursor places = resolver.query(Places.CONTENT_URI, null, null, null, null);

        LiberationModel model = new LiberationModel();

        // Jumps
        if (jumps != null ) {
            model.jumps = new Jump[jumps.getCount()];
            while (jumps.moveToNext()) {
                Jump jump = new Jump();
                jump.number = jumps.getInt(jumps.getColumnIndex(Jumps.JUMP_NUMBER));
                jump.date = jumps.getLong(jumps.getColumnIndex(Jumps.JUMP_DATE));
                jump.way = jumps.getInt(jumps.getColumnIndex(Jumps.JUMP_WAY));
                jump.place = jumps.getInt(jumps.getColumnIndex(Jumps.PLACE_ID));
                jump.jumpType = jumps.getInt(jumps.getColumnIndex(Jumps.JUMPTYPE_ID));
                jump.exitAltitude = jumps.getInt(jumps.getColumnIndex(Jumps.JUMP_EXIT_ALTITUDE));
                jump.deploymentAltitude = jumps.getInt(jumps.getColumnIndex(Jumps.JUMP_DEPLOYMENT_ALTITUDE));
                jump.delay = jumps.getInt(jumps.getColumnIndex(Jumps.JUMP_DELAY));
                jump.description = jumps.getString(jumps.getColumnIndex(Jumps.JUMP_DESCRIPTION));
                model.jumps[jumps.getPosition()] = jump;
            }
            jumps.close();
        }

        // JumpTypes
        if (jumpTypes != null ) {
            model.jumpTypes = new JumpType[jumpTypes.getCount()];
            while (jumpTypes.moveToNext()) {
                JumpType jumpType = new JumpType();
                jumpType.id = jumpTypes.getInt(jumpTypes.getColumnIndex(JumpTypes._ID));
                jumpType.name = jumpTypes.getString(jumpTypes.getColumnIndex(JumpTypes.JUMPTPYE_NAME));
                model.jumpTypes[jumpTypes.getPosition()] = jumpType;
            }
            jumpTypes.close();
        }

        // Places
        if (places != null) {
            model.places = new Place[places.getCount()];
            while (places.moveToNext()) {
                Place place = new Place();
                place.id = places.getInt(places.getColumnIndex(Places._ID));
                place.name = places.getString(places.getColumnIndex(Places.PLACE_NAME));
                place.longitude = places.getDouble(places.getColumnIndex(Places.PLACE_LONGITUDE));
                place.latitude = places.getDouble(places.getColumnIndex(Places.PLACE_LATITUDE));
                model.places[places.getPosition()] = place;
            }
            places.close();
        }

        return (new GsonBuilder().setPrettyPrinting().create()).toJson(model);
    }
}
