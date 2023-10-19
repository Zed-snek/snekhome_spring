package ed.back_snekhome.manualTest;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TestRepository extends JpaRepository<TestEntity, Long> {

    @Query("SELECT MAX(t.id) FROM TestEntity t ")
    Long getLastId();

    @Query("SELECT t.name FROM TestEntity t WHERE t.id = (SELECT MIN(t2.id) FROM TestEntity t2)")
    String getFirstName();
}
