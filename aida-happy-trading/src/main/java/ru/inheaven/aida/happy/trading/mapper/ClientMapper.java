package ru.inheaven.aida.happy.trading.mapper;

import org.apache.ibatis.session.SqlSession;
import ru.inheaven.aida.happy.trading.entity.Client;

import javax.inject.Inject;
import java.util.List;

/**
 * @author inheaven on 24.06.2015 16:11.
 */
public class ClientMapper{
    @Inject
    private SqlSession sqlSession;

    @Inject
    private AccountMapper accountMapper;

    public List<Client> getClients(){
        return sqlSession.selectList("selectClients");
    }

    public Client getClient(Long id){
        return sqlSession.selectOne("selectClient", id);
    }

    public void save(Client client){
        if (client.getId() == null) {
            sqlSession.insert("insertClient", client);

            client.getAccounts().forEach(accountMapper::save);
        }else {
            sqlSession.update("updateClient", client);

            getClient(client.getId()).getAccounts().forEach(a -> {
                        if (!client.getAccounts().stream()
                                .filter(a1 -> a.getId().equals(a1.getId()))
                                .findAny()
                                .isPresent()) {
                            accountMapper.delete(a);
                        }
                    }
            );

            client.getAccounts().forEach(a -> {
                a.setClientId(client.getId());
                accountMapper.save(a);
            });
        }
    }

}
