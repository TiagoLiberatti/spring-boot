package com.tiagodev.cursomc.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.tiagodev.cursomc.domain.Cidade;
import com.tiagodev.cursomc.domain.Cliente;
import com.tiagodev.cursomc.domain.Endereco;
import com.tiagodev.cursomc.domain.enums.TipoCliente;
import com.tiagodev.cursomc.dto.ClienteDTO;
import com.tiagodev.cursomc.dto.ClienteNewDTO;
import com.tiagodev.cursomc.repositories.ClienteRepository;
import com.tiagodev.cursomc.repositories.EnderecoRepository;
import com.tiagodev.cursomc.services.exceptions.DataIntegrityException;
import com.tiagodev.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClienteService {

	@Autowired
	private ClienteRepository repo;
	@Autowired
	private EnderecoRepository enderecoRepository;
	
	public Cliente find(Integer id) {
		Optional<Cliente> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				 "Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName()));
	}
	
	public Cliente update(Cliente obj) {
		Cliente newObj = find(obj.getId());
		updateData(newObj, obj);
		return repo.save(newObj);		
	}
	
	public void delete(Integer id) {
		find(id);
		try {
			repo.deleteById(id);	
		}catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir porque há entidades relacionadas");
		}	
	}
	
	public List<Cliente> findAll() {
		return repo.findAll();
	}
	
	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction){
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}
	
	public Cliente fromDto(ClienteDTO dto) {
		return new Cliente(dto.getId(), dto.getNome(), dto.getEmail(), null, null);
	}
	
	public Cliente fromDto(ClienteNewDTO dto) {
		Cliente cliente = new Cliente(null, dto.getNome(), dto.getEmail(), dto.getCpfOuCnpj(), TipoCliente.toEnum(dto.getTipo()));
		Cidade cidade = new Cidade(dto.getCidadeId(), null, null);
		Endereco end = new Endereco(null, dto.getLogradouro(), dto.getNumero(), dto.getComplemento(), dto.getBairro(), dto.getCep(), cliente, cidade);
		cliente.getEnderecos().add(end);
		cliente.getTelefones().add(dto.getTelefone1());
		if(dto.getTelefone2() != null) {
			cliente.getTelefones().add(dto.getTelefone2());
		}
		if(dto.getTelefone3() != null) {
			cliente.getTelefones().add(dto.getTelefone3());
		}
		return cliente;
	}
	
	private void updateData(Cliente newObj, Cliente obj) {
		newObj.setNome(obj.getNome());
		newObj.setEmail(obj.getEmail());
	}
	
	public Cliente insert(Cliente obj) {
		obj.setId(null);
		obj = repo.save(obj);
		enderecoRepository.saveAll(obj.getEnderecos());
		return obj;		
	}
	
}