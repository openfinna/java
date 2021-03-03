package org.openfinna.java.connector.parser;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openfinna.java.connector.classes.ResourceInfo;
import org.openfinna.java.connector.classes.models.libraries.*;
import org.openfinna.java.connector.classes.models.libraries.schedule.Day;
import org.openfinna.java.connector.classes.models.libraries.schedule.Schedule;
import org.openfinna.java.connector.classes.models.resource.Author;
import org.openfinna.java.connector.classes.models.resource.Format;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FinnaJSONParser {

    private static final String IMG_TEMPLATE = "https://www.finna.fi/Cover/Show?recordid=%s&isbn=%s";
    private static final SimpleDateFormat scheduleDateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public static ResourceInfo parseResourceInfo(JSONObject jsonObject) {
        // Init params
        List<Author> authors = new ArrayList<>();
        List<Format> formats = new ArrayList<>();
        JSONObject rawData = jsonObject.optJSONObject("rawData");

        String id = jsonObject.optString("id", null);
        String title = jsonObject.optString("title", null);
        String subTitle = jsonObject.optString("subTitle", null);
        String shortTitle = jsonObject.optString("shortTitle", null);
        String isbn = jsonObject.optString("cleanIsbn", null);
        String edition = jsonObject.optString("edition", null);
        String manufacturer = jsonObject.optString("manufacturer", null);
        int publicationYear = jsonObject.optInt("year", 1970);
        JSONArray pdArray = jsonObject.optJSONArray("physicalDescription");
        JSONArray ppArray = jsonObject.optJSONArray("placesOfPublication");
        String physicalDescription = (pdArray != null && pdArray.length() > 0) ? pdArray.optString(0, null) : null;
        String publicationPlace = (ppArray != null && ppArray.length() > 0) ? ppArray.optString(0, null) : null;
        String imageLink = String.format(IMG_TEMPLATE, id, isbn);
        List<String> topics = new ArrayList<>();
        if (jsonObject.has("subjects")) {
            List<List<String>> rawTopics = new Gson().fromJson(jsonObject.optJSONArray("subjects").toString(), TypeToken.getParameterized(List.class, TypeToken.getParameterized(List.class, String.class).getType()).getType());
            for (List<String> topic : rawTopics) {
                topics.addAll(topic);
            }
        }
        // Parse string arrays
        List<String> generalNotes = parseJSONStringArray("generalNotes", jsonObject);
        List<String> languages = parseJSONStringArray("languages", jsonObject);
        List<String> originalLanguages = parseJSONStringArray("originalLanguages", jsonObject);
        List<String> publishers = parseJSONStringArray("publishers", jsonObject);
        List<String> awards = parseJSONStringArray("awards", jsonObject);
        List<String> ykl = parseJSONStringArray("ykl", jsonObject.optJSONObject("classifications"));

        // Parse authors and formats
        JSONObject authorsTypeObject = jsonObject.optJSONObject("authors");
        if (authorsTypeObject != null) {
            Iterator<String> typeIterator = authorsTypeObject.keys();
            while (typeIterator.hasNext()) {
                String type = typeIterator.next();
                JSONObject authorsObject = authorsTypeObject.optJSONObject(type);
                if (authorsObject != null) {
                    Iterator<String> authorsIterator = authorsObject.keys();
                    while (authorsIterator.hasNext()) {
                        String author = authorsIterator.next();
                        List<String> roles = new ArrayList<>();
                        JSONObject authorObject = authorsObject.optJSONObject(author);
                        if (authorObject.has("role")) {
                            List<String> tempRoles = parseJSONStringArray("role", authorObject);
                            if (tempRoles.size() > 0 && !tempRoles.get(0).equals("-")) {
                                roles.addAll(tempRoles);
                            }
                        }
                        authors.add(new Author(author, roles, type));
                    }
                }
            }
        }
        JSONArray formatsJSONArray = jsonObject.optJSONArray("formats");
        for (int i = 0; i < formatsJSONArray.length(); i++) {
            JSONObject formatObject = formatsJSONArray.optJSONObject(i);
            if (formatObject != null) {
                String formatId = formatObject.optString("value");
                String formatType = formatObject.optString("translated");
                if (formats.stream().noneMatch(f -> f.getTranslated().equals(formatType))) {
                    formats.add(new Format(formatId, formatType));
                }
            }
        }
        return new ResourceInfo(id, title, shortTitle, subTitle, topics, publicationYear, isbn, authors, formats, generalNotes, languages, originalLanguages, physicalDescription, edition, manufacturer, publishers, publicationPlace, ykl, awards, imageLink, rawData);
    }

    public static Library parseLibrary(JSONObject libraryJSON) throws ParseException {
        String id = libraryJSON.optString("id");
        String name = libraryJSON.optString("name");
        String shortName = libraryJSON.optString("shortName");
        String slug = libraryJSON.optString("slug");
        String slogan = libraryJSON.optString("slogan");
        boolean currentlyOpen = libraryJSON.optBoolean("openNow");
        List<Link> links = new ArrayList<>();
        List<Image> images = new ArrayList<>();
        List<String> services = parseJSONStringArray("services", libraryJSON);
        List<String> scheduleNotices = parseJSONStringArray("scheduleDescriptions", libraryJSON);
        if (libraryJSON.has("links")) {
            List<Link> parsedLinks = new Gson().fromJson(libraryJSON.getJSONArray("links").toString(), TypeToken.getParameterized(List.class, Link.class).getType());
            links.addAll(parsedLinks);
        }
        if (libraryJSON.has("pictures")) {
            List<Image> parsedImages = new Gson().fromJson(libraryJSON.getJSONArray("pictures").toString(), TypeToken.getParameterized(List.class, Image.class).getType());
            images.addAll(parsedImages);
        }
        LibraryType type = LibraryType.MUNICIPAL;
        if (libraryJSON.optString("type", "").equals("mobile"))
            type = LibraryType.MOBILE;
        String email = libraryJSON.optString("email", "");
        String homepage = libraryJSON.optString("homepage", "");
        LibraryLocation libraryLocation = null;
        if (libraryJSON.has("address")) {
            JSONObject address = libraryJSON.optJSONObject("address");

            LibraryLocation.Coordinates coordinates = null;
            JSONObject coordinatesObject = address.optJSONObject("coordinates");
            if (coordinatesObject != null) {
                coordinates = new LibraryLocation.Coordinates(coordinatesObject.optDouble("lat"), coordinatesObject.optDouble("lon"));
            }
            libraryLocation = new LibraryLocation(address.optString("street"), address.optString("zipcode"), address.optString("city"), libraryJSON.optString("mapUrl"), libraryJSON.optString("routeUrl"), coordinates);
        }
        List<Day> librarySchedule = new ArrayList<>();
        if (libraryJSON.has("openTimes") && libraryJSON.optJSONObject("openTimes").has("schedules"))
            librarySchedule.addAll(parseLibrarySchedule(libraryJSON.optJSONObject("openTimes").optJSONArray("schedules")));
        return new Library(id, name, shortName, slug, type, email, homepage, libraryLocation, images, links, services, scheduleNotices, slogan, librarySchedule, currentlyOpen);
    }

    public static List<Day> parseLibrarySchedule(JSONArray scheduleJSON) throws ParseException {
        List<Day> days = new ArrayList<>();
        for (int i = 0; i < scheduleJSON.length(); i++) {
            JSONObject day = scheduleJSON.optJSONObject(i);
            Date date = scheduleDateFormat.parse(day.optString("date", "01.01.") + Calendar.getInstance().get(Calendar.YEAR));
            boolean closed = day.optBoolean("closed", false);
            Schedule schedule = null;
            if (!closed) {
                JSONObject times = day.optJSONArray("times").optJSONObject(0);
                int opensHour = times.optInt("opens");
                int closesHour = times.optInt("closes");
                boolean selfService = times.optBoolean("selfService");

                Calendar opensCalendar = Calendar.getInstance();
                opensCalendar.setTime(date);
                opensCalendar.set(Calendar.HOUR, opensHour);
                opensCalendar.set(Calendar.MINUTE, 0);

                Calendar closesCalendar = Calendar.getInstance();
                closesCalendar.setTime(date);
                closesCalendar.set(Calendar.HOUR, closesHour);
                closesCalendar.set(Calendar.MINUTE, 0);
                schedule = new Schedule(opensCalendar.getTime(), closesCalendar.getTime(), selfService);
            }
            days.add(new Day(date, closed, schedule));
        }
        return days;
    }

    public static List<Library> parseLibraries(JSONArray librariesJSON) throws ParseException {
        List<Library> libraries = new ArrayList<>();
        for (int i = 0; i < librariesJSON.length(); i++) {
            libraries.add(parseLibrary(librariesJSON.optJSONObject(i)));
        }
        return libraries;
    }

    public static List<ResourceInfo> parseResourceInfos(JSONArray resourcesJSON) {
        List<ResourceInfo> resourceInfos = new ArrayList<>();
        for (int i = 0; i < resourcesJSON.length(); i++) {
            resourceInfos.add(parseResourceInfo(resourcesJSON.optJSONObject(i)));
        }
        return resourceInfos;
    }

    private static List<String> parseJSONStringArray(String key, JSONObject jsonObject) {
        List<String> array = new ArrayList<>();
        if (jsonObject != null && jsonObject.has(key)) {
            JSONArray jsonArray = jsonObject.optJSONArray(key);
            for (int i = 0; i < jsonArray.length(); i++) {
                array.add(jsonArray.optString(i, ""));
            }
        }
        return array;
    }
}
