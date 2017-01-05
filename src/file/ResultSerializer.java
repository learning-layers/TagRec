package file;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Map;

public class ResultSerializer {

	public static void serializePredictions(Map<Integer, Map<Integer, Double>> predictions, String filePath) {
		OutputStream file = null;
		try {
			file = new FileOutputStream(filePath);
			OutputStream buffer = new BufferedOutputStream(file);
			ObjectOutput output = new ObjectOutputStream(buffer);
			output.writeObject(predictions);
			output.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
