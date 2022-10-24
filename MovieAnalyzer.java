import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MovieAnalyzer {

  Supplier<Stream<Movie>> streamSupplier;

  public static class Movie {
    private final String link;
    private final String title;
    private final int year;
    private final String certificate;
    private final int runtime;
    private final String genre;
    private final float rating;
    private final String overview;
    private final int score;
    private final String director;
    private final String star1;
    private final String star2;
    private final String star3;
    private final String star4;
    private final int votes;
    private final int gross;

    public Movie(
        String link,
        String title,
        int year,
        String certificate,
        int runtime,
        String genre,
        float rating,
        String overview,
        int score,
        String director,
        String star1,
        String star2,
        String star3,
        String star4,
        int votes,
        int gross) {
      this.link = link;
      this.title = title;
      this.year = year;
      this.certificate = certificate;
      this.runtime = runtime;
      this.genre = genre;
      this.rating = rating;
      this.overview = overview;
      this.score = score;
      this.director = director;
      this.star1 = star1;
      this.star2 = star2;
      this.star3 = star3;
      this.star4 = star4;
      this.votes = votes;
      this.gross = gross;
    }

    public String getLink() {
      return link;
    }

    public String getTitle() {
      return title;
    }

    public int getYear() {
      return year;
    }

    public String getCertificate() {
      return certificate;
    }

    public int getRuntime() {
      return runtime;
    }

    public String getGenre() {
      return genre;
    }

    public float getRating() {
      return rating;
    }

    public String getOverview() {
      return overview;
    }

    public int getScore() {
      return score;
    }

    public String getDirector() {
      return director;
    }

    public String getStar1() {
      return star1;
    }

    public String getStar2() {
      return star2;
    }

    public String getStar3() {
      return star3;
    }

    public String getStar4() {
      return star4;
    }

    public int getVotes() {
      return votes;
    }

    public int getGross() {
      return gross;
    }
  }

  public MovieAnalyzer(String data_set) {

    streamSupplier =
        () -> {
          try {
            return Files.lines(Paths.get(data_set))
                .skip(1)
                .map(
                    l -> {
                      if (l.endsWith(",")) {
                        l += "0";
                      }
                      return l.trim().split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                    })
                .map(
                    a ->
                        new Movie(
                            a[0],
                            a[1].replace("\"", ""),
                            Integer.parseInt(a[2]),
                            a[3],
                            Integer.parseInt(a[4].replace(" min", "")),
                            a[5],
                            Float.parseFloat(a[6]),
                            a[7].replace("\"\"", "''").replace("\"", ""),
                            Integer.parseInt(a[8].replace("", "0")),
                            a[9],
                            a[10],
                            a[11],
                            a[12],
                            a[13],
                            Integer.parseInt(a[14]),
                            Integer.parseInt(a[15].replace(",", "").replace("\"", ""))));
          } catch (IOException e) {
            e.printStackTrace();
          }
          return null;
        };
  }

  public Map<Integer, Integer> getMovieCountByYear() {
    Map<Integer, Long> moviePerYear =
        streamSupplier
            .get()
            .map(Movie::getYear)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    Map<Integer, Integer> result =
        new TreeMap<>((o1, o2) -> -o1.compareTo(o2)); // Sort by key in reversed order.
    for (Map.Entry<Integer, Long> entry : moviePerYear.entrySet()) {
      int value = Integer.parseInt(String.valueOf(entry.getValue()));
      result.put(entry.getKey(), value);
    }
    return result;
  }

  public Map<String, Integer> getMovieCountByGenre() {
    List<Movie> movieList = streamSupplier.get().collect(Collectors.toList());
    Map<String, Integer> genreNum = new HashMap<>();
    for (Movie movie : movieList) {
      String[] rawGenre = movie.getGenre().replace("\"", "").split(", ");
      for (String genre : rawGenre) {
        if (genreNum.containsKey(genre)) {
          int num = genreNum.get(genre);
          num++;
          genreNum.put(genre, num);
        } else {
          genreNum.put(genre, 1);
        }
      }
    }
    Map<String, Integer> sorted = new LinkedHashMap<>();
    genreNum.entrySet().stream()
        .sorted(
            Map.Entry.<String, Integer>comparingByValue()
                .reversed()
                .thenComparing(Map.Entry.comparingByKey()))
        .forEachOrdered(e -> sorted.put(e.getKey(), e.getValue()));
    return sorted;
  }

  public Map<List<String>, Integer> getCoStarCount() {
    List<Movie> movieList = streamSupplier.get().collect(Collectors.toList());
    Map<List<String>, Integer> coStarNum = new HashMap<>();
    for (Movie movie : movieList) {
      String[] stars =
          new String[] {movie.getStar1(), movie.getStar2(), movie.getStar3(), movie.getStar4()};
      Arrays.sort(stars);
      for (int i = 0; i < 3; i++) {
        for (int j = i + 1; j < 4; j++) {
          List<String> coStars = new ArrayList<>();
          coStars.add(stars[i]);
          coStars.add(stars[j]);
          if (coStarNum.containsKey(coStars)) {
            int num = coStarNum.get(coStars);
            num++;
            coStarNum.put(coStars, num);
          } else {
            coStarNum.put(coStars, 1);
          }
        }
      }
    }
    return coStarNum;
  }

  public List<String> getTopMovies(int top_k, String by) {
    List<String> topMovies = new ArrayList<>();
    List<Movie> movieList = streamSupplier.get().collect(Collectors.toList());
    if (by.equals("runtime")) {
      movieList =
          movieList.stream()
              .sorted(
                  Comparator.comparing(Movie::getRuntime).reversed().thenComparing(Movie::getTitle))
              .collect(Collectors.toList());
      for (int i = 0; i < top_k; i++) {
        topMovies.add(movieList.get(i).getTitle());
      }
    } else if (by.equals("overview")) {
      Map<String, Integer> movieOverview = new HashMap<>();
      for (Movie movie : movieList) {
        movieOverview.put(movie.getTitle(), movie.getOverview().length());
      }
      Map<String, Integer> sorted = new LinkedHashMap<>();
      movieOverview.entrySet().stream()
          .sorted(
              Map.Entry.<String, Integer>comparingByValue()
                  .reversed()
                  .thenComparing(Map.Entry.comparingByKey()))
          .forEachOrdered(e -> sorted.put(e.getKey(), e.getValue()));
      topMovies.addAll(sorted.keySet());
      topMovies = topMovies.subList(0, top_k);
    }
    return topMovies;
  }

  public List<String> getTopStars(int top_k, String by) {
    List<Movie> movieList = streamSupplier.get().collect(Collectors.toList());
    List<String> topStars = new ArrayList<>();
    if (by.equals("rating")) {
      Map<String, double[]> starInfo = new LinkedHashMap<>();
      for (Movie movie : movieList) {
        String[] stars =
            new String[] {movie.getStar1(), movie.getStar2(), movie.getStar3(), movie.getStar4()};
        for (String star : stars) {
          double[] info;
          if (starInfo.containsKey(star)) {
            info = starInfo.get(star);
          } else {
            info = new double[3];
          }
          info[0] += movie.getRating();
          info[1] += 1;
          info[2] = info[0] / info[1];
          starInfo.put(star, info);
        }
      }
      Map<String, Double> ratingAvg = new HashMap<>();
      for (Map.Entry<String, double[]> entry : starInfo.entrySet()) {
        String star = entry.getKey();
        double avgRating = entry.getValue()[2];
        ratingAvg.put(star, avgRating);
      }
      Map<String, Double> sorted = new LinkedHashMap<>();
      ratingAvg.entrySet().stream()
          .sorted(
              Map.Entry.<String, Double>comparingByValue()
                  .reversed()
                  .thenComparing(Map.Entry.comparingByKey()))
          .forEachOrdered(e -> sorted.put(e.getKey(), e.getValue()));
      topStars.addAll(sorted.keySet());
      topStars = topStars.subList(0, top_k);
    } else if (by.equals("gross")) {
      Map<String, long[]> starInfo = new LinkedHashMap<>();
      for (Movie movie : movieList) {
        String[] stars =
            new String[] {movie.getStar1(), movie.getStar2(), movie.getStar3(), movie.getStar4()};
        for (String star : stars) {
          long[] info;
          if (starInfo.containsKey(star)) {
            info = starInfo.get(star);
          } else {
            info = new long[3];
          }
          if (movie.getGross() != 0) {
            info[0] += movie.getGross();
            info[1] += 1;
            info[2] = info[0] / info[1];
          }
          starInfo.put(star, info);
        }
      }
      Map<String, Long> grossAvg = new HashMap<>();
      for (Map.Entry<String, long[]> entry : starInfo.entrySet()) {
        String star = entry.getKey();
        long avgRating = entry.getValue()[2];
        grossAvg.put(star, avgRating);
      }
      Map<String, Long> sorted = new LinkedHashMap<>();
      grossAvg.entrySet().stream()
          .sorted(
              Map.Entry.<String, Long>comparingByValue()
                  .reversed()
                  .thenComparing(Map.Entry.comparingByKey()))
          .forEachOrdered(e -> sorted.put(e.getKey(), e.getValue()));
      topStars.addAll(sorted.keySet());
      topStars = topStars.subList(0, top_k);
      return topStars;
    }
    return topStars;
  }

  public List<String> searchMovies(String genre, float min_rating, int max_runtime) {
    List<Movie> movieList = streamSupplier.get().collect(Collectors.toList());
    List<String> result = new ArrayList<>();
    for (Movie movie : movieList) {
      boolean condition1 = false;
      boolean condition2 = false;
      boolean condition3 = false;

      String[] rawGenre = movie.getGenre().replace("\"", "").split(", ");
      for (String gn : rawGenre) {
        if (gn.equals(genre)) {
          condition1 = true;
          break;
        }
      }

      float rating = movie.getRating();
      if (rating >= min_rating) {
        condition2 = true;
      }

      int runtime = movie.getRuntime();
      if (runtime <= max_runtime) {
        condition3 = true;
      }

      if (condition1 && condition2 && condition3) {
        result.add(movie.getTitle());
      }
    }
    Collections.sort(result);
    return result;
  }

  public static void main(String[] args) {
    // Testing area
    MovieAnalyzer m = new MovieAnalyzer("resources/imdb_top_500.csv");
    System.out.println(m.getTopStars(20, "gross"));
  }
}
