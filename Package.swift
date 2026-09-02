// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SolanaKotlin",
    platforms: [
        .iOS(.v17)
    ],
    products: [
        .library(
            name: "SolanaKotlin",
            targets: ["SolanaKotlin"]
        ),
    ],
    targets: [
        .binaryTarget(
            name: "SolanaKotlin",
            url: "https://github.com/avianlabs/solana-kotlin/releases/download/0.5.0/SolanaKotlin.zip",
            checksum: "c1d49d6b7f9edf590af385d6197cefd158a915626ad806efc0e07aa17793c4c6"
        ),
    ]
)
