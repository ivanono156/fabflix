DELIMITER //

CREATE PROCEDURE add_movie(
    IN movie_title VARCHAR(100),
    IN movie_year INT,
    IN movie_director VARCHAR(100),
    IN star_name VARCHAR(100),
    IN genre_name VARCHAR(32),
    OUT new_movie_id VARCHAR(10),
    OUT new_star_id VARCHAR(10),
    OUT new_genre_id INT,
    OUT status_message VARCHAR(255)  -- Added output parameter for status message
)
BEGIN
    DECLARE movie_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INT;

    -- Check if the movie already exists
    SELECT id INTO movie_id
    FROM movies
    WHERE title = movie_title AND year = movie_year AND director = movie_director
    LIMIT 1;

    IF movie_id IS NOT NULL THEN
        -- Set the status message
        SET status_message = 'failure';
    ELSE
        -- Create and insert the new movie
        SELECT MAX(id) INTO movie_id FROM movies;
        SET new_movie_id = CONCAT('tt', CAST(SUBSTRING(movie_id, 3) AS UNSIGNED) + 1);

        INSERT INTO movies (id, title, year, director, price)
        VALUES (new_movie_id, movie_title, movie_year, movie_director, floor(10 + rand() * 100 - 10));

        -- Check if the star already exists
        SELECT id INTO star_id
        FROM stars
        WHERE name = star_name
        LIMIT 1;

        IF star_id IS NULL THEN
            -- Create and insert the new star
            SELECT MAX(id) INTO star_id FROM stars;
            SET new_star_id = CONCAT('nm', CAST(SUBSTRING(star_id, 3) AS UNSIGNED) + 1);

            INSERT INTO stars (id, name)
            VALUES (new_star_id, star_name);
        ELSE
            SET new_star_id = star_id;
        END IF;

        INSERT INTO stars_in_movies (starID, movieId)
        VALUES (new_star_id, new_movie_id);

        -- Check if the genre exists
        SELECT id INTO genre_id
        FROM genres
        WHERE name = genre_name
        LIMIT 1;

        IF genre_id IS NULL THEN
            -- Create the new genre
            INSERT INTO genres (name)
            VALUES (genre_name);
            SET new_genre_id = LAST_INSERT_ID();
        ELSE
            SET new_genre_id = genre_id;
        END IF;

        INSERT INTO genres_in_movies (genreId, movieId)
        VALUES (new_genre_id, new_movie_id);

        SET status_message = 'success';  -- Set success message
    END IF;
END //

DELIMITER ;
