package in.tech_camp.task_checker_back.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import in.tech_camp.task_checker_back.entity.GenreEntity;

@Mapper
public interface GenreRepository {

  @Select("SELECT * FROM genres")
  List<GenreEntity> findAll();

  @Select("SELECT * FROM genres WHERE id = #{id}")
  GenreEntity findById(Integer id);

  @Insert("INSERT INTO genres (name) VALUES (#{name})")
  @Options(useGeneratedKeys = true, keyProperty = "id")
  void insert(GenreEntity genre);

  @Delete("DELETE FROM genres WHERE id = #{id}")
  void deleteById(Integer id);
}
