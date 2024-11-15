package br.com.pedrodouglas.gerarexcel.repository;

import br.com.pedrodouglas.gerarexcel.model.Nfe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NfeRepository extends JpaRepository<Nfe, Long> {
}
