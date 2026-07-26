#include "xchain/xchain.h"
#include <map>
#include <vector>
#include <string>

using namespace std;
using namespace xchain;

// 资产信息结构体
struct AssetInfo {
    string file_hash;
    string ipfs_cid;
    string creator;
    string organization;
    int64_t timestamp;
    string tx_id;
};

// 双桶索引：哈希->资产信息
map<string, AssetInfo> map_hash_to_asset;
// 双桶索引：用户ID->资产哈希列表
map<string, vector<string>> map_user_to_assets;

// 存证函数
bool save_evidence(const string& user_id, const AssetInfo& asset) {
    // 检查哈希是否已存在
    if (map_hash_to_asset.find(asset.file_hash) != map_hash_to_asset.end()) {
        return false;
    }
    
    // 写入哈希索引
    map_hash_to_asset[asset.file_hash] = asset;
    
    // 写入用户索引
    map_user_to_assets[user_id].push_back(asset.file_hash);
    
    // 触发存证事件
    xchain::emit_event("EvidenceSaved", {
        {"user_id", user_id},
        {"file_hash", asset.file_hash},
        {"tx_id", asset.tx_id},
        {"timestamp", to_string(asset.timestamp)}
    });
    
    return true;
}

// 按哈希查询资产
AssetInfo get_asset_by_hash(const string& file_hash) {
    if (map_hash_to_asset.find(file_hash) != map_hash_to_asset.end()) {
        return map_hash_to_asset[file_hash];
    }
    return AssetInfo();
}

// 按用户查询资产列表
vector<string> get_assets_by_user(const string& user_id) {
    if (map_user_to_assets.find(user_id) != map_user_to_assets.end()) {
        return map_user_to_assets[user_id];
    }
    return vector<string>();
}

// 合约入口
extern "C" int32_t initialize() {
    return 0;
}

extern "C" int32_t invoke() {
    string method = xchain::get_method();
    
    if (method == "save_evidence") {
        string user_id = xchain::get_arg("user_id");
        AssetInfo asset;
        asset.file_hash = xchain::get_arg("file_hash");
        asset.ipfs_cid = xchain::get_arg("ipfs_cid");
        asset.creator = xchain::get_arg("creator");
        asset.organization = xchain::get_arg("organization");
        asset.timestamp = stoll(xchain::get_arg("timestamp"));
        asset.tx_id = xchain::get_arg("tx_id");
        
        bool result = save_evidence(user_id, asset);
        xchain::set_response(result ? "success" : "failed");
    } else if (method == "get_asset_by_hash") {
        string file_hash = xchain::get_arg("file_hash");
        AssetInfo asset = get_asset_by_hash(file_hash);
        xchain::set_response(json::serialize(asset));
    } else if (method == "get_assets_by_user") {
        string user_id = xchain::get_arg("user_id");
        vector<string> assets = get_assets_by_user(user_id);
        xchain::set_response(json::serialize(assets));
    }
    
    return 0;
}