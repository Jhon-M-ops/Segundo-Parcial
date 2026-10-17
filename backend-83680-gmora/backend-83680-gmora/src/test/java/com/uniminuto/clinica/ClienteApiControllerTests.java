package com.uniminuto.clinica;

import com.uniminuto.clinica.apicontroller.ClienteApiController;
import com.uniminuto.clinica.entity.Cliente;
import com.uniminuto.clinica.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class ClienteApiControllerTests {

    @Test
    void listarClientesReturnsClientJsonWithoutRequestContentType() throws Exception {
        Cliente cliente = new Cliente();
        cliente.setClienteId(1L);
        cliente.setTipoDocumento("CC");
        cliente.setNumeroDocumento("123456");
        cliente.setNombres("Ana");
        cliente.setApellidos("Pérez");

        ClienteService clienteService = mock(ClienteService.class);
        when(clienteService.listarClientes()).thenReturn(List.of(cliente));

        MockMvc mockMvc = standaloneSetup(new ClienteApiController(clienteService))
                .build();

        mockMvc.perform(get("/cliente/listar").accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clienteId").value(1))
                .andExpect(jsonPath("$[0].nombres").value("Ana"))
                .andExpect(jsonPath("$[0].apellidos").value("Pérez"));
    }
}
