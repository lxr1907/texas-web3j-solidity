pragma solidity ^0.4.18;
contract owned {
    address public owner;
    function owned() public {
        owner = msg.sender;
    }
    modifier onlyOwner {
        require(msg.sender == owner);
        _;
    }
    function transferOwnership(address newOwner) onlyOwner public {
        owner = newOwner;
    }
}
contract TexasContract is owned{
    //18 decimals 1ETH=10^18 wei
    uint8 constant decimals = 18;
    //合约拥有者
    address owner;
    //所有者奖励
    uint256 ownerFee;
    //所有者奖励比例千分之50,既5%
    uint256 ownerFeeRate=50;
    //0.01个ETH最小充值
    uint256 minBet=(10**uint256(decimals))/100;
    //0.1个ETH最大充值
    uint256 maxBet=(10**uint256(decimals))/10;
    
    // 在区块链上创建一个公共事件，它触发就会通知所有客户端
    event Transfer(address indexed from, address indexed to, uint256 value);
    event TransferError(address indexed from, address indexed to, uint256 value);
    event Bonus(address indexed from, uint256 value);
    event Whithdraw(address indexed from, uint256 value);
    struct player{
        //奖池
        uint256 bonus;
        //充值次数
        uint256 times;
        //win
        uint256 bonusWin;
    }
    //创建所有账户余额数组
    mapping (address => player) players;
    address[]  playersArray;
    /**
     * 初始化合约
     */
    function TexasContract(
    ) public {
        //初始化合约所有人
        owner=msg.sender;             
    }
    /// 使用以太坊换筹码
    function () payable public {
        uint amount = msg.value;
        require(amount>=minBet);
        //require(amount<=maxBet);
        addToArray(msg.sender);
        players[msg.sender].times+=1;
        players[msg.sender].bonus+=amount;
        //通知
        Bonus(msg.sender,amount);
       
    }
    //将该地址加入数组
    function addToArray(address _player) internal{
        //如果不存在，将该地址加入数组，用于以后遍历访问
        if(players[msg.sender].times==0){
            playersArray.push(_player);   
        }
    }
    //管理员根据游戏结果，将筹码从一个用户转移到另一个
    function bonusTransfer(address _playerWin,address _playerLose,uint amount) onlyOwner public{
        require(amount>0);
        //输的玩家金额足够
        require(players[_playerLose].bonus>=amount);
        //手续费
        uint ownerFeePlus=amount/1000*ownerFeeRate;
        ownerFee=ownerFee+ownerFeePlus;
        uint loseOld=players[_playerLose].bonus;
        uint winOld=players[_playerWin].bonus;
        players[_playerLose].bonus-=amount;
        players[_playerWin].bonus+=amount-ownerFeePlus;
        if(players[_playerLose].bonus+players[_playerWin].bonus==loseOld+winOld-ownerFeePlus){
            //成功通知
            Transfer(_playerLose,_playerWin,amount);
        }else{
            //失败数据回退
            players[_playerLose].bonus=loseOld;
            players[_playerWin].bonus=winOld;
            //失败通知
            TransferError(_playerLose,_playerWin,amount);
        }
    }
    //用户将筹码从一个用户转移到另一个
    function bonusTransfer(address _playerWin,uint amount) public{
        address _playerLose=msg.sender;
        require(amount>0);
        //输的玩家金额足够
        require(players[_playerLose].bonus>=amount);
        //手续费
        uint ownerFeePlus=amount/1000*ownerFeeRate;
        ownerFee=ownerFee+ownerFeePlus;
        uint loseOld=players[_playerLose].bonus;
        uint winOld=players[_playerWin].bonus;
        players[_playerLose].bonus-=amount;
        players[_playerWin].bonus+=amount-ownerFeePlus;
        if(players[_playerLose].bonus+players[_playerWin].bonus==loseOld+winOld-ownerFeePlus){
            //成功通知
            Transfer(_playerLose,_playerWin,amount);
        }else{
            //失败数据回退
            players[_playerLose].bonus=loseOld;
            players[_playerWin].bonus=winOld;
            //失败通知
            TransferError(_playerLose,_playerWin,amount);
        }
    }
    /**
     * 用户提取ETH
     */
    function whithdraw(uint amount)public{
        require(amount<=players[msg.sender].bonus);
        if(amount<=0){
            amount=players[msg.sender].bonus;
        }
        uint _bonus=players[msg.sender].bonus;
        players[msg.sender].bonus=players[msg.sender].bonus-amount;
        if(_bonus==players[msg.sender].bonus+amount){
            msg.sender.transfer(_bonus);
            //提现通知
            Whithdraw(msg.sender,amount);
        }
    }
    /**
     * 用户获取可提现金额
     */
    function canWhithdraw() public view returns(uint256 _bonus){
       _bonus= players[msg.sender].bonus;
    }
    /**
     * 管理员提取ETH手续费
     */
    function whithdrawAdmin() onlyOwner public{
        require(this.balance>=ownerFee);
        uint _ownerFee=ownerFee;
        ownerFee=0;
        owner.transfer(_ownerFee);
    }
    /**
     * 管理员设置手续费千分率
     */
    function setRate(uint rate) onlyOwner public {
        ownerFeeRate=rate;
    }
}