package com.learn.restaurants.infrastructure;

import com.learn.restaurants.domain.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface RestaurantRepositoryDAO extends JpaRepository<Restaurant, Long> {
    @Query(value = """
    SELECT *
    FROM restaurants
    WHERE ST_DWithin(
        CAST(location AS geography),
        CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography),
        :radiusMeters
    )
    AND (:cuisine IS NULL OR cuisine = :cuisine)
    AND (:minRating IS NULL OR rating >= :minRating)
    ORDER BY ST_Distance(
        CAST(location AS geography),
        CAST(ST_SetSRID(ST_MakePoint(:longitude, :latitude), 4326) AS geography)
    )
    LIMIT :limit
    """, nativeQuery = true)
    List<Restaurant> findNearByRestaurants(@Param("longitude") double longitude,
                                           @Param("latitude") double latitude,
                                           @Param("radiusMeters") double radiusMeters,
                                           @Param("limit") int limit,
                                           @Param("cuisine") String cuisine,
                                           @Param("minRating") Double minRating);


}
