package com.community.jpremium.common.util;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.time.Instant;

public class InstantEpochTypeAdapter extends TypeAdapter<Instant> {
    @Override
    public void write(JsonWriter jsonWriter, Instant instant) throws IOException {
        jsonWriter.value(instant.getEpochSecond());
    }

    @Override
    public Instant read(JsonReader jsonReader) throws IOException {
        return Instant.ofEpochSecond(jsonReader.nextLong());
    }
}
