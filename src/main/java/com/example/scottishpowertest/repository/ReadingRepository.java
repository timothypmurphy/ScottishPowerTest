package com.example.scottishpowertest.repository;

import com.example.scottishpowertest.domain.ReadingType;
import com.example.scottishpowertest.entities.Reading;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ReadingRepository extends JpaRepository<Reading, Long> {
    List<Reading> findAllByAccountIdAndReadingTypeOrderByDateAsc(Integer id, ReadingType readingType);
    Reading findFirstByAccountIdAndReadingTypeOrderByDateDesc(Integer id, ReadingType readingType);
    Reading findFirstByAccountIdAndReadingTypeOrderByDateAsc(Integer id, ReadingType readingType);
    List<Reading> findAllByReadingTypeOrderByDateDesc(ReadingType readingType);
}

