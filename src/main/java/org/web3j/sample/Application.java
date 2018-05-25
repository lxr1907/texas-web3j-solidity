package org.web3j.sample;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.sample.contracts.generated.Greeter;
import org.web3j.sample.contracts.generated.Texas;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

/**
 * A simple web3j application that demonstrates a number of core features of
 * web3j:
 *
 * <ol>
 * <li>Connecting to a node on the Ethereum network</li>
 * <li>Loading an Ethereum wallet file</li>
 * <li>Sending Ether from one address to another</li>
 * <li>Deploying a smart contract to the network</li>
 * <li>Reading a value from the deployed smart contract</li>
 * <li>Updating a value in the deployed smart contract</li>
 * <li>Viewing an event logged by the smart contract</li>
 * </ol>
 *
 * <p>
 * To run this demo, you will need to provide:
 *
 * <ol>
 * <li>Ethereum client (or node) endpoint. The simplest thing to do is
 * <a href="https://infura.io/register.html">request a free access token from
 * Infura</a></li>
 * <li>A wallet file. This can be generated using the web3j
 * <a href="https://docs.web3j.io/command_line.html">command line tools</a></li>
 * <li>Some Ether. This can be requested from the
 * <a href="https://www.ropsten.io/#faucet">ropsten Faucet</a></li>
 * </ol>
 *
 * <p>
 * For further background information, refer to the project README.
 */
public class Application {

	private static final Logger log = LoggerFactory.getLogger(Application.class);

	public static void main(String[] args) throws Exception {
		new Application().run();
	}

	private void run() throws Exception {
		String account2 = "0xdc834D429b3098f0568Af873c2d73b08790BF677";
		String account3 = "0x1D24B15eBdEf8a2444A30b95C102E7dD8d800B04";
		// We start by creating a new web3j instance to connect to remote nodes
		// on the network.
		// Note: if using web3j Android, use Web3jFactory.build(...
		Web3j web3j = Web3j.build(new HttpService("https://ropsten.infura.io/zJAdv5eyBNqcUCxwXWf8")); // FIXME:
																										// Enter
																										// your
																										// Infura
																										// token
																										// here;
		log.info("Connected to Ethereum client version: " + web3j.web3ClientVersion().send().getWeb3ClientVersion());

		// We then need to load our Ethereum wallet file
		// FIXME: Generate a new wallet file using the web3j command line tools
		// https://docs.web3j.io/command_line.html
		// C:\Users\aac\AppData\Roaming\Ethereum\testnet\keystore
		Credentials credentials = WalletUtils.loadCredentials("382697973",
				"C://Users//aac//AppData//Roaming//Ethereum//testnet//keystore//UTC--2018-05-24T09-38-13.506000000Z--1d24b15ebdef8a2444a30b95c102e7dd8d800b04.json");
		log.info("Credentials loaded");

		// FIXME: Request some Ether for the ropsten test network at
		// https://www.ropsten.io/#faucet
		log.info("Sending 1 Wei (" + Convert.fromWei("1", Convert.Unit.ETHER).toPlainString() + " Ether)");

		// Now lets deploy a smart contract
		log.info("Deploying smart contract 创建合约");
		// 创建合约
		// Texas contract = Texas.deploy(web3j, credentials,
		// ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT).send();
		String contractAddress = "0x35441adabc362a0081c78c26f431821db654db98";

		Texas contract = Texas.load(contractAddress, web3j, credentials, DefaultGasProvider.GAS_PRICE,
				DefaultGasProvider.GAS_LIMIT);
		// 获取合约地址
		String texasAddress = contract.getContractAddress();
		log.info("Smart contract loaded 获取合约地址：" + texasAddress);
		log.info("View contract at https://ropsten.etherscan.io/address/" + texasAddress);

		BigInteger canWithdraw = contract.canWhithdraw(account3).send();
		log.info("canWithdraw:" + canWithdraw);
		TransactionReceipt transferReceipt = Transfer
				.sendFunds(web3j, credentials, texasAddress, new BigDecimal(10000000000000000l), Convert.Unit.WEI)
				.send();
		canWithdraw = contract.canWhithdraw(account3).send();
		log.info("canWithdraw:" + canWithdraw);
		log.info("Transaction complete, view it at https://ropsten.etherscan.io/tx/"
				+ transferReceipt.getTransactionHash());

		TransactionReceipt transferReceipt2 = contract.bonusTransfer(account3, account2,
				Convert.toWei(new BigDecimal(0.01), Convert.Unit.ETHER).toBigInteger()).send();
		// 监听事件
		for (Texas.TransferEventResponse event : contract.getTransferEvents(transferReceipt)) {
			log.info("from: " + event.from + ", to: " + event.to + ",amount" + event.value);
		}
		for (Texas.BonusEventResponse event : contract.getBonusEvents(transferReceipt2)) {
			log.info("from: " + event.from + ",amount" + event.value);
		}
	}
}
