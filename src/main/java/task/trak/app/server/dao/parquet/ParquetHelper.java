package task.trak.app.server.dao.parquet;

import task.trak.app.client.cli.TTApp;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.parquet.avro.AvroParquetReader;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.LocalInputFile;
import org.apache.parquet.io.LocalOutputFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParquetHelper {

    public static List<GenericRecord> readAll(String fileName, Schema schema) {
        List<GenericRecord> records = new ArrayList<>();
        File file = new File(TTApp.storedir + File.separator + fileName);
        if (!file.exists()) return records;

        try (ParquetReader<GenericRecord> reader = AvroParquetReader.<GenericRecord>builder(
                        new LocalInputFile(file.toPath()))
                .build()) {
            GenericRecord record;
            while ((record = reader.read()) != null) {
                records.add(record);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return records;
    }

    public static void writeAll(String fileName, Schema schema, List<GenericRecord> records) {
        File file = new File(TTApp.storedir + File.separator + fileName);
        if (file.exists()) {
            file.delete();
        }

        try (ParquetWriter<GenericRecord> writer = AvroParquetWriter.<GenericRecord>builder(
                        new LocalOutputFile(file.toPath()))
                .withSchema(schema)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .build()) {
            for (GenericRecord record : records) {
                writer.write(record);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
