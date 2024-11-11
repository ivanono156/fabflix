DELIMITER //

CREATE PROCEDURE add_movie(
    IN movie_title VARCHAR(255),
    IN movie_year INT,
    IN movie_director VARCHAR(255),
    IN star_name VARCHAR(255),
    IN genre_name VARCHAR(255),
    OUT new_movie_id VARCHAR(10),
    OUT new_star_id VARCHAR(10),
    OUT new_genre_id INT,
    OUT status_message VARCHAR(255)  -- Added output parameter for status message
)
BEGIN
    DECLARE m_id VARCHAR(10);
    DECLARE movie_id VARCHAR(10);
    DECLARE s_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);
    DECLARE g_id INT;
    DECLARE genre_id VARCHAR(10);

    -- Check if the movie already exists
    SELECT id INTO movie_id
    FROM movies
    WHERE title = movie_title AND year = movie_year AND director = movie_director
    LIMIT 1;

    IF movie_id IS NOT NULL THEN
        SET status_message = 'Movie already exists';  -- Set the status message
    ELSE

        -- Insert the new movie
        SELECT MAX(id) INTO movie_id FROM movies;
        SET m_id = CONCAT('tt', LPAD(CAST(SUBSTRING(movie_id, 3) AS UNSIGNED) + 2, 7, '0'));
        WHILE EXISTS (SELECT 1 FROM movies WHERE id = m_id) DO
                SET m_id = CONCAT('tt', LPAD(CAST(SUBSTRING(m_id, 3) AS UNSIGNED) + 1, 7, '0'));
            END WHILE;

        INSERT INTO movies (id, title, year, director)
        VALUES (m_id, movie_title, movie_year, movie_director);

        SET new_movie_id = m_id;

        SELECT id INTO star_id
        FROM stars
        WHERE name = star_name
        LIMIT 1;

        IF star_id IS NULL THEN
            SELECT MAX(id) INTO star_id FROM stars;
            SET s_id = CONCAT('nm', LPAD(CAST(SUBSTRING(star_id, 3) AS UNSIGNED) + 2, 7, '0'));
            WHILE EXISTS (SELECT 1 FROM stars WHERE id = s_id) DO
                    SET s_id = CONCAT('nm', LPAD(CAST(SUBSTRING(s_id, 3) AS UNSIGNED) + 1, 7, '0'));
                END WHILE;
            SET new_star_id = s_id;
            INSERT INTO stars (id, name)
            VALUES (s_id, star_name);
        ELSE
            SET s_id = star_id;
        END IF;

        INSERT INTO stars_in_movies (starID, movieId)
        VALUES (s_id, movie_id);

        SELECT id INTO genre_id
        FROM genres
        WHERE name = genre_name
        LIMIT 1;
        IF genre_id IS NULL THEN
            INSERT INTO genres (name)
            VALUES (genre_name);
            SET genre_id = LAST_INSERT_ID();
        END IF;
        INSERT INTO genres_in_movies (genreId, movieId)
        VALUES (genre_id, movie_id);

        SET status_message = 'Movie added successfully';  -- Set success message
    END IF;
END //

DELIMITER ;
