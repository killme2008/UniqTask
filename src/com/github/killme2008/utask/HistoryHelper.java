package com.github.killme2008.utask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

/**
 * Helper for save kill apps history
 * 
 * @author apple
 * 
 */
public class HistoryHelper {

	private static String fileName = "kill_apps.history";

	/**
	 * Load kill history from file
	 * 
	 * @param ctx
	 * @return
	 */
	public List<String> loadKilledPackageNames(Context ctx) {
		List<String> rt = new ArrayList<String>();
		FileInputStream in = null;
		BufferedReader reader = null;
		try {

			in = ctx.openFileInput(fileName);
			reader = new BufferedReader(new InputStreamReader(in));
			String pkgName = null;
			while ((pkgName = reader.readLine()) != null) {
				rt.add(pkgName);
			}
		} catch (FileNotFoundException e) {
			// ignore
		} catch (IOException e) {
			Log.e(ProcessList.LOG_TAG, e.getMessage());
		} finally {
			if (reader != null) {
				close(reader);
			}
			if (in != null) {
				close(in);
			}
		}
		return rt;
	}

	private void close(Closeable closeable) {
		try {
			closeable.close();
		} catch (IOException e) {
			// ignore
		}
	}

	/**
	 * Save killed history to file
	 * 
	 * @param ctx
	 * @param pkgNames
	 */
	public void saveKilledPackageNames(Context ctx, List<String> pkgNames) {
		FileOutputStream out = null;
		BufferedWriter writer = null;

		try {
			out = ctx.openFileOutput(fileName, Context.MODE_WORLD_WRITEABLE);
			writer = new BufferedWriter(new OutputStreamWriter(out));
			for (String pkgName : pkgNames) {
				writer.write(pkgName);
				writer.newLine();
			}

		} catch (FileNotFoundException e) {
			// ignore
		} catch (IOException e) {
			Log.e(ProcessList.LOG_TAG, e.getMessage());
		} finally {
			if (writer != null) {
				close(writer);
			}
			if (out != null) {
				close(out);
			}
		}
	}
}
