package one.oth3r.directionhud.common.utils;

import one.oth3r.directionhud.common.DHUD;
import one.oth3r.directionhud.common.files.config;
import one.oth3r.directionhud.utils.CTxT;
import one.oth3r.directionhud.utils.Player;
import one.oth3r.directionhud.utils.Utl;

import java.util.*;

public class Helper {
    public static final int MAX_NAME = 16;
    public static class Enums {
        public static <T extends Enum<T>> ArrayList<T> toArrayList(T[] array) {
            return new ArrayList<>(Arrays.asList(array));
        }
        public static <T extends Enum<T>> ArrayList<String> toStringList(ArrayList<T> enumList) {
            ArrayList<String> stringList = new ArrayList<>();
            for (T entry:enumList) stringList.add(entry.toString());
            return stringList;
        }
        public static <T extends Enum<T>> ArrayList<T> toEnumList(ArrayList<String> stringList, Class<T> enumType, boolean... setting) {
            boolean settingMode = setting != null;
            ArrayList<T> moduleList = new ArrayList<>();
            for (String module:stringList) {
                try {
                    T enumValue = Enum.valueOf(enumType, module);
                    if (settingMode) enumValue = Enum.valueOf(enumType, module.replace(".","__"));
                    moduleList.add(enumValue);
                } catch (IllegalArgumentException ignored) {}
            }
            return moduleList;
        }
        @SafeVarargs
        public static <T extends Enum<T>> T next(T current, Class<T> enumType, T... exclude) {
            T[] values = enumType.getEnumConstants();
            T next = values[(current.ordinal()+1)%values.length];
            if (exclude != null) {
                for (T item:exclude)
                    if (item.equals(next))
                        return next(next,enumType,exclude);
            }
            return next;
        }
        /**
         * gets an enum from a string without returning null
         * @param enumString the string of the enum
         * @param enumType the class of enums
         * @return an enum, if there isn't a match, it returns the first enum
         */
        public static <T extends Enum<T>> T get(Object enumString, Class<T> enumType) {
            T[] values = enumType.getEnumConstants();
            for (T all : values) {
                // check if there is a match for any of the enum names
                if (enumString.toString().equals(all.name())) return all;
            }
            // if there's no match return the first entry
            return values[0];
        }
    }
    public static class Num {
        public static boolean isInt(String string) {
            try {
                Integer.parseInt(string);
            } catch (NumberFormatException nfe) {
                return false;
            }
            return true;
        }
        public static Integer toInt(String s) {
            // return an int no matter what
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                try {
                    return (int) Double.parseDouble(s);
                } catch (NumberFormatException e2) {
                    return 0;
                }
            }
        }
        public static boolean isNum(String s) {
            // checks if int or a double
            try {
                Integer.parseInt(s);
                return true;
            } catch (NumberFormatException e1) {
                try {
                    Double.parseDouble(s);
                    return true;
                } catch (NumberFormatException e2) {
                    return false;
                }
            }
        }
        public static boolean inBetween(double num, double min, double max) {
            // if min is greater than max, flip
            if (min > max) return num >= min || num <= max;
            return num >= min && num <= max;
        }
        public static double wSubtract(double num, double sub, double max) {
            // wrapped subtract
            double output = num - sub;
            if (output < 0) output = max - (output*-1);
            return output;
        }
        public static double wAdd(double num, double add, double max) {
            // wrapped add
            return (num+add)%max;
        }
    }
    public static class Command {
        public static class Suggester {
            public static <T>ArrayList<String> wrapQuotes(List<T> list) {
                ArrayList<String> output = new ArrayList<>();
                for (T s:list) {
                    output.add("\""+s+"\"");
                }
                return output;
            }
            public static String getCurrent(String[] args, int pos) {
                // if the length is the same as the pos, there's nothing currently being typed at that pos
                // get the current typed message
                if (args.length == pos+1) return args[pos];
                return "";
            }
            public static ArrayList<String> xyz(Player player, String current, int type) {
                // type = 3: all 3, ect
                ArrayList<String> list = new ArrayList<>();
                if (type == 3) {
                    list.add(String.valueOf(player.getBlockX()));
                    list.add(player.getBlockX()+" "+player.getBlockZ());
                    list.add(player.getBlockX()+" "+player.getBlockY()+" "+player.getBlockZ());
                } else if (type == 2) {
                    list.add(String.valueOf(player.getBlockY()));
                    list.add(player.getBlockY()+" "+player.getBlockZ());
                } else if (type == 1) list.add(String.valueOf(player.getBlockZ()));
                // don't suggest if typing letters or different coordinates
                if (current.isEmpty() || list.get(0).startsWith(current)) return list;
                return new ArrayList<>();
            }
            public static String name() {
                return "\"name\"";
            }
            public static ArrayList<String> players(Player player) {
                //get player strings excluding the inputted player
                ArrayList<String> list = new ArrayList<>();
                for (Player entry: Utl.getPlayers())
                    if (!entry.equals(player)) list.add(entry.getName());
                return list;
            }
            public static ArrayList<String> dims(String current) {
                return dims(current,true);
            }
            public static ArrayList<String> dims(String current, boolean displayEmpty) {
                ArrayList<String> list = new ArrayList<>();
                if (!current.isEmpty() || displayEmpty) {
                    if (current.isEmpty()) return Dim.getAll();
                    for (String dim : Dim.getAll()) {
                        if (dim.contains(current)) list.add(dim);
                    }
                }
                return list;
            }
            public static ArrayList<String> colors(Player player, String current) {
                return colors(player,current,true);
            }
            public static ArrayList<String> colors(Player player, String current, boolean displayEmpty) {
                ArrayList<String> list = new ArrayList<>();
                ArrayList<String> presets = new ArrayList<>();
                // add all presets to a list
                for (String name : DHUD.preset.custom.getNames(player.getPData().getColorPresets()))
                    presets.add(String.format("\"preset-%s\"",name));
                // displaying logic, if not empty and not display empty or empty and display empty
                if (!current.isEmpty() || displayEmpty) {
                    list.add("ffffff");
                    if (!presets.isEmpty() && Utl.checkEnabled.customPresets(player)) {
                        list.add("preset");
                        if (current.startsWith("pre")) {
                            list.addAll(presets);
                            return list;
                        }
                    }
                    list.add("red");
                    list.add("orange");
                    list.add("yellow");
                    list.add("green");
                    list.add("blue");
                    list.add("purple");
                    list.add("gray");
                }
                return list;
            }
        }
        public static String[] quoteHandler(String[] args) {
            // put quoted items all in one arg
            boolean quote = false;
            ArrayList<String> output = new ArrayList<>();
            StringBuilder addBuffer = new StringBuilder();
            for (String s : args) {
                s = removeSpecial(s); // remove all special characters
                if (s.contains("\"")) {
                    int count = s.length() - s.replaceAll("\"","").length(); // count how many quotes there are
                    s = s.replace("\"",""); // remove the quote
                    if (quote) { // end of quote, don't skip the loop to add to the arraylist
                        quote = false;
                        addBuffer.append(" ").append(s);
                    } else if (count == 1) { // start of the quote, skip the rest of the loop to fill the buffer
                        quote = true;
                        addBuffer.append(s);
                        continue;
                    } else addBuffer.append(s); // all in one quote, add to buffer directly
                } else if (quote) { // middle of the quote, add a space and skip the rest of the loop
                    addBuffer.append(" ").append(s);
                    continue;
                } else addBuffer.append(s); // no loop just add to buffer
                output.add(addBuffer.toString()); // dump the buffer
                addBuffer.setLength(0); // clear the builder
            }
            // make sure if the buffer still has something then dump it to the output array
            if (!addBuffer.isEmpty()) output.add(addBuffer.toString());
            String[] arr = new String[output.size()];
            return output.toArray(arr);
        }
    }
    public static String createID() {
        //spigot not having apache smh my head
        String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        Random random = new Random();
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            int randomIndex = random.nextInt(CHARS.length());
            char randomChar = CHARS.charAt(randomIndex);
            sb.append(randomChar);
        }
        return sb.toString();
    }
    public static String[] trimStart(String[] arr, int numToRemove) {
        if (numToRemove > arr.length) return new String[0];
        String[] result = new String[arr.length - numToRemove];
        System.arraycopy(arr, numToRemove, result, 0, result.length);
        return result;
    }
    /**
     * removes any special characters from the inputted string
     * @return stripped string
     */
    public static String removeSpecial(String input) {
        return input.replaceAll("[\\\\|{}\\[\\]()]|[,;:'\\s]", "");
    }
    public static class Pair<A, B> {
        private final A first;
        private final B second;
        public Pair(A first, B second) {
            this.first = first;
            this.second = second;
        }
        public String toString() {
            return "("+this.first+", "+this.second+")";
        }
        public Pair<B,A> getFlipped() {
            return new Pair<>(second,first);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Pair<?, ?> otherPair = (Pair<?, ?>) obj;
            return Objects.equals(first, otherPair.first) && Objects.equals(second, otherPair.second);
        }
        @Override
        public int hashCode() {
            return Objects.hash(first, second);
        }
    }
    public static class ListPage<T> {
        // helps separate lists into page sized chunks
        private final ArrayList<T> list;
        private final int perPage;
        public ListPage(ArrayList<T> list, int perPage) {
            this.list = list;
            this.perPage = perPage;
        }
        public int getTotalPages() {
            // get max pages, min = 1
            return Math.max(1,(int) Math.ceil((double) list.size() / perPage));
        }
        public ArrayList<T> getList() {
            return list;
        }
        public int getPageOf(T item) {
            // get the quotient of the index and the amount of items per page rounded to the next integer to get page of the current item
            if (list.contains(item)) return (int) Math.ceil((double) (list.indexOf(item) + 1) / perPage);
            else return 1;
        }
        public int getIndexOf(T item) {
            return list.indexOf(item);
        }
        public ArrayList<T> getPage(int page) {
            //return a list with the entries in the page given
            int max = getTotalPages();
            if (max < page) page = max;
            if (page <= 0) page = 1;
            ArrayList<T> pageList = new ArrayList<>();
            // loop for amount per page
            for (int i = 0;i < perPage;i++) {
                // get the current index, (page-1) * amt per page + current page index
                int index = (page-1)*perPage+i;
                if (list.size() > index) pageList.add(list.get(index));
            }
            return pageList;
        }
        public CTxT getNavButtons(int page, String command) {
            // return the buttons to change page
            int max = getTotalPages();
            if (page > max) page = max;
            if (page < 2) page = 1;
            CTxT left = CTxT.of("");
            CTxT right = CTxT.of("");
            // if at the start left is gray else not
            if (page==1) left.append(CTxT.of("<<").btn(true).color('7'));
            else left.append(CTxT.of("<<").btn(true).color(CUtl.s()).cEvent(1,command+(page-1)));
            // if at the end right is gray else not
            if (page==max) right.append(CTxT.of(">>").btn(true).color('7'));
            else right.append(CTxT.of(">>").btn(true).color(CUtl.s()).cEvent(1,command+(page+1)));
            // build and return
            return CTxT.of("")
                    .append(left).append(" ")
                    .append(CTxT.of(String.valueOf(page)).btn(true).color(CUtl.p()).cEvent(2,command).hEvent(CUtl.hover("page_set").color(CUtl.p())))
                    .append(" ").append(right);
        }
    }
    public static class Dim {
        private static final String DIMENSION = "dimension";
        private static final String COLOR = "color";
        private static final String NAME = "name";
        private static final List<HashMap<String,String>> dimensions = new ArrayList<>();
        private static final HashMap<Pair<String, String>, Double> conversionRatios = new HashMap<>();
        /**
         * returns all dimension names
         * @return list with dimension names
         */
        public static ArrayList<String> getAll() {
            ArrayList<String> names = new ArrayList<>();
            for (HashMap<String, String> dimension : dimensions)
                names.add(dimension.get(DIMENSION));
            return names;
        }
        /**
         * gets the formatted name of the dimension specified
         * @param dimName the dimension
         * @return the dimension's name
         */
        public static String getName(String dimName) {
            return dimensions.stream()
                    .filter(dimension -> dimension.get(DIMENSION).equals(dimName))
                    .map(dimension -> dimension.get(NAME))
                    .findFirst().orElse("unknown");
        }
        /**
         * gets the HEX color for the dimension specified
         * @param dimName the dimension
         * @return HEX color
         */
        public static String getColor(String dimName) {
            return dimensions.stream()
                    .filter(dimension -> dimension.get(DIMENSION).equals(dimName))
                    .map(dimension -> dimension.get(COLOR))
                    .findFirst().orElse("#FF0000");
        }
        /**
         * makes a one letter badge of the dimension, eg [O] for overworld
         * @param dimName the dimension
         * @return the badge
         */
        public static CTxT getBadge(String dimName) {
            // find the entry
            HashMap<String, String> entry = dimensions.stream()
                    .filter(dimension -> dimension.get(DIMENSION).equals(dimName))
                    .findFirst().orElse(new HashMap<>());
            // if not found
            if (entry.isEmpty()) return CTxT.of("X").btn(true).hEvent(CTxT.of("???"));
            // make the badge
            return CTxT.of(String.valueOf(entry.get(NAME).charAt(0)).toUpperCase()).btn(true)
                    .color(entry.get(COLOR)).hEvent(CTxT.of(entry.get(NAME)).color(entry.get(COLOR)));
        }
        /**
         * checks if the two dimension's coordinates can be converted to each other
         * @param dimensionA dimension one
         * @param dimensionB dimension two
         * @return if the dimensions can be converted or not
         */
        public static boolean canConvert(String dimensionA, String dimensionB) {
            // both in same dim, cant convert
            if (dimensionA.equalsIgnoreCase(dimensionB)) return false;
            Pair<String, String> key = new Pair<>(dimensionA, dimensionB);
            // if the ratio exists (both normal and flipped), it's true
            return conversionRatios.containsKey(key) || conversionRatios.containsKey(key.getFlipped());
        }
        /**
         * gets the ratio for 2 dimensions, flipped accordingly
         * @param dimensionFrom the from dimension
         * @param dimensionTo the to dimension
         * @return the ratio as a double
         */
        public static double getRatio(String dimensionFrom, String dimensionTo) {
            Pair<String, String> dimensionPair = new Helper.Pair<>(dimensionFrom, dimensionTo), flippedPair = dimensionPair.getFlipped();
            if (conversionRatios.containsKey(dimensionPair)) return conversionRatios.get(dimensionPair);
            // flip the ratio if there's a flipped ratio
            else if (conversionRatios.containsKey(flippedPair)) return 1 / conversionRatios.get(flippedPair);
            // no ratio if no match
            else return 1.0;
        }
        /**
         * checks if the dimension is in the list of dimensions
         * @return if the dimension exists in the list or not
         */
        public static boolean checkValid(String dimName) {
            return dimensions.stream()
                    .anyMatch(dimension -> dimension.get(DIMENSION).equals(dimName));
        }
        public static void load() {
            Utl.dim.addMissing();
            loadConfig();
            loadConfigRatios();
        }
        private static void loadConfigRatios() {
            conversionRatios.clear();

        }
        private static void loadConfig() {
            dimensions.clear();
            dimensions.addAll(config.dimensions);
            conversionRatios.clear();
            for (HashMap<String,Double> entry : config.dimensionRatios) {
                String[] dims = entry.keySet().toArray(new String[0]);
                double ratio = entry.get(dims[0]) / entry.get(dims[1]);
                conversionRatios.put(new Pair<>(dims[0],dims[1]), ratio);
            }
        }

        /**
         * returns a dimension list that is fixed for error handling
         * @param list the list to fix
         * @return the fixed list
         */
        public static ArrayList<HashMap<String,String>> fixDimensions(ArrayList<HashMap<String,String>> list) {
            ArrayList<HashMap<String,String>> output = new ArrayList<>();
            for (HashMap<String,String> map : list) {
                if (map == null) continue;
                if (!map.containsKey(DIMENSION) || !map.containsKey(COLOR) || !map.containsKey(NAME)) continue;
                if (map.get(NAME).length() > MAX_NAME) continue;
                map.put(COLOR,CUtl.color.format(map.get(COLOR)));
                output.add(map);
            }
            return output;
        }
        public static ArrayList<HashMap<String,Double>> fixRatios(ArrayList<HashMap<String,Double>> list) {
            // todo
            return list;
        }
        public static ArrayList<HashMap<String,Double>> loadLegacyRatios(List<String> oldList) {
            ArrayList<HashMap<String,Double>> out = new ArrayList<>();
            for (String s : oldList) {
                String[] entries = s.split("\\|");
                if (entries.length != 2) continue;
                String[] entry1 = entries[0].split("="), entry2 = entries[1].split("=");
                HashMap<String,Double> entry = new HashMap<>();
                entry.put(entry1[0],Double.parseDouble(entry1[1]));
                entry.put(entry2[0],Double.parseDouble(entry2[1]));
                out.add(entry);
            }
            return out;
        }
        public static ArrayList<HashMap<String,String>> loadLegacyDimensions(List<String> oldList) {
            ArrayList<HashMap<String,String>> list = new ArrayList<>();
            // for all config dimensions
            for (String entry : oldList) {
                String[] entries = entry.split("\\|");
                // if not correct length
                if (entries.length != 3) continue;
                // filling data
                HashMap<String,String> data = new HashMap<>();
                data.put(DIMENSION,entries[0]);
                data.put(NAME,entries[1]);
                data.put(COLOR,entries[2]);
                // add the dimension
                list.add(data);
            }
            return list;
        }
    }
}
