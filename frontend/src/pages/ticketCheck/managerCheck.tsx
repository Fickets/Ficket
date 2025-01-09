import React, { useEffect, useRef, useState } from "react";
import { useNavigate, useParams, useLocation } from "react-router-dom";
import { Client } from '@stomp/stompjs';
import { SocketMessage } from "../../types/ticketCheck";

const ManagerCheckPage: React.FC = () => {
    const { eventId } = useParams<{ eventId: string }>();
    const location = useLocation();

    const queryParams = new URLSearchParams(location.search);
    const connectId = queryParams.get("connectId");

    const [client, setClient] = useState<Client | null>(null)

    const [socketMessage, setSocketMessage] = useState<SocketMessage>();

    useEffect(() => {
        const stored = localStorage.getItem('ADMIN_STORE');

        if (stored) {
            const obj = JSON.parse(stored);

            if (obj.state.accessToken !== '') {
                const token = obj.state.accessToken

                // WebSocket 연결 설정
                const connectionOptions = {
                    // brokerURL: 'wss://k10d202.p.ssafy.io/api/ws',
                    brokerURL: 'ws://localhost:9000/ticketing-check/ws',
                    connectHeaders: {
                        Authorization: token
                    }, // 연결 시 헤더 설정

                    onConnect: () => {
                        newClient.subscribe(
                            `/sub/check/${eventId}/${connectId}`,
                            message => {
                                try {
                                    // JSON 메시지를 파싱
                                    const parsedMessage: SocketMessage = JSON.parse(message.body);
                                    console.log(parsedMessage)
                                    // 상태 업데이트
                                    setSocketMessage(parsedMessage);
                                } catch (error) {
                                    console.error("Failed to parse message or invalid data", error);
                                }


                                // setMessages(prev => [...prev, parsedMessage]);
                            },
                        );
                    },

                    onDisconnect: () => { }
                };
                const newClient = new Client();

                newClient.configure(connectionOptions);

                // 웹소켓 세션 활성화
                newClient.activate();
                setClient(newClient);
                return () => {
                    newClient.deactivate();
                };
            }
        }
    }, []);
    // 웹소켓 연결

    return (

        <div className="flex flex-col items-center justify-center bg-gray-400 ">
            <div className="mt-[50px] flex justify-between ">
                <p className="font-bold text-[30px]">Ficket Manager</p>
                <span className="ml-[15px] font-semibold text-[30px]">{connectId}</span>
            </div>
            <div className="mt-[50px] mx-[10px]">
                <img
                    src="data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBw8QEhAQEA8PDxIVFhYVEBAPDw8PDxAPFhEWFxUSFhUYHSggGBolHRUVITEhJSkrLi4uFx8zODMsNygtLisBCgoKDg0OGhAQFy0dHSUtLS0tLS0rLS0rLS0tLSstLS0rLS0rLS0tLS0tLS0tKy0tKy0tLS0tLS0rLS0tLS0tLf/AABEIAQMAwwMBIgACEQEDEQH/xAAcAAABBAMBAAAAAAAAAAAAAAAAAQIDBAUGBwj/xABCEAACAQICBgcFBQYEBwAAAAABAgADEQQhBQYSMUFRB2FxgZGhsRMiMkLBUnKC0fAUI2KSsuEkM6LCFRZDZIPi8f/EABkBAQADAQEAAAAAAAAAAAAAAAABAgMEBf/EACMRAQEAAgICAgIDAQAAAAAAAAABAhEhMQMyEkEEURQicRP/2gAMAwEAAhEDEQA/AOywhCWBEhEMAhCJAIQhAIQmK1l0/QwFFq1Y9SICNqo/BR+fCBkq9ZKal6jqiLmzuwVVHMk7pqWkOkrRlI7K1TWO69NTsfzfkDOPa1a2YrSD3rPZASadBMqacsuJ/iOc1qri9nLPxvClyehtG9JGjazFGqmieBqA+zawvkw49RAvwvKmN6VdH0yQiYisB8yoiqezaa/jaefqbMxLcBLFyRnfwko+bv2i+lHRdZgrvUwpO44hLU/51JCjra03SlUVgGVgykXVlIZSDuII3ieTATxPju8JtGpeuuI0Y4UXq4cm74ct7uZzemT8LceR4840mZPRsJS0NpahjKKYjDuHptuPFWG9GHBhxEuyFxCEIBFiQgLeLEhAWEIQFiRYkAMSBhASEIQCEIQIsViUpI9WowREUs7HcqgXJM8766axVNI4hqputNbrQT7FO+8j7R3nuHATo/TDpjZp0sEhzqH2lYA/9JT7inqLAn/x9c45iK1slz6+Z6oVqKoLDeB1neZjalics/KT+xeq1hc8yOM3fVjUc1AGcED6SLlIY+O59Na0bgGKsuycxe9pBVw9SnvnZ8HqrTpiwA3WvYXmP0xqoHuQP1YTP/o3/j8d8uUB1Is3paQ1Et1rwPIzPad1XqUrsASBy5TA0ze6nfy5/wB5rMpenPnhcbqtp6OdbW0bibVGP7NVIXEL8qH5awHMceYuM7Lb0UDfdn2TyOx58Mj1jnO/dD+njicEKNRr1cMfZm+80bXpN3C6/gkmN+m9whCQuIQhAIsSLAIQhAWBhEMAiQhAIQhAIQmE100mMLgsTWvZtgpT5mo/uLbvN+6Bw7XPS5xeLxFVSSGYhDwWgvupblcAG3Mma64+UZm/nJ6z7CknNvrMtqPos4iqWIuBlf1kW6m0Y4/K6Z3UvVbaKu4y3nrnVcFg1RQAAJV0TgwgAAtMwqzHvmuzjGaiIUxI61ES0ViFMpKNtW0xgFYEWnItatEmhU2wMif1528J3LGpvnPNfMMDSY8Rulcbqp8kmWDmGItcHgcvHMTc+h7TH7PpCnTY2SurUW5e0HvU2PepH45peL3eH9REZh8S1J1qIbOjK6Hk6kMp8QJ0vP3qvXUJXwGKWtTp1VIIdVYW3Zi8nkNSxY2LAWEIQFhEiwFiRYkBIQMIBCESAs4/0z6yI1Sng6bXFK717ZgViLKnWwXay/j6jOraSxBpUa1RbXRGZb7rhSReeXtNX9qSxZmJLMWPvGoxJZj157++FcrpQxVYk57uXHvnWuirBgYZals2J9Zx5xczunRaP8HS7WH+synl6X/H9r/jZWoYpvganRXgTd3PXyEqey0ihv8AtVKoPstTEk0r7ZnRA7UqV/3jou0+z1cB3g9k1HD4PH0cVWYV6poLtMu03tRVJZdhAozAttZm1rzOOq9zh0PAV6rL+9UK3HZ+E9kMZXZQdkbR4C9hE0RVL0wzAqSAdlsiMtx65Xx1SzACN8GudMJicNpGrc+3pURyCXy6zNO1twOJp0n2661RbO62PdMrrXo/HYtUNOrUoXuGpFnVaYv7rn2ZuxtzJG8W4jVtZKVSmaqU/amgRkKpYlSBvG1mBfnJ/XKt+5qtIqm9+z6kyqTulojNvu/nKY4TdwV6V6KsWamjcODcGmNgX+wACp8DNvnPehI/4Bt/+aQb33imv0tOhSGs6EWJCEnQiRYBCEIDjEgYQEhCEBIQiQGV6YZWU/MCPEWnl/WtCmIrKd4IT+VQPpPUc839JmF2NIYscPaBh2Oob6wrl01WmmajiTO19GDWw+wcirsCO2x+s4eKhBBG8bu206r0V6ep1qj0gpSpsBmW4KGzWJU7+IyPnKeXpp+NZuus+zHKR/s6nOwlilukdVrTN1FsBMRjalqol1sSoZA7hS2SKd5ImI09VBqKqMNsDa2eJUbzIqcZyyz0gRewmq636PVsPWsBfYJFhyF5tdP4RMNpzNHB5GKmfp57JzcdRHr/AGlJdw7ZbxxUVX2dwYjztKnMToeZXfOg2tfCV04rWBtyDUkt/SZ0icY6B9IgVMRQJ+NFYC/zIT9GPlOzQ0nRYQhCRHRsUQFhCEBTEixIBEMWIYCQhCATi3TTo0riRWAyq0lN+dRCVPlsTtM0LphwgbC0qlrlahXudCT5oIo89NMrqZpUYPHUKzGybWxVO4Cm/uknqFwfwzHYhLEjrtK1VczFm4yxvxu3rbDtcCK4HGa3qLjzUweGLm5NNMzx90eczmOwi1lKPcqeRI6xmJg9D7R47FU0BBYXtu3mYg4qiWFit+N95I7d8xelNVwM1NTqIqOD6zFf8ruzC7VLcWao5PrKWuuePD47mTeKeIuJrmuWkVo4erUY7lPeeA8ZksPTShTVFyCjKck6TNYfbVP2am11Q3qEbi/Be7f225S2M3dOXPKYy1pCuTe+9jc9pjm840LnHnhOl5trbejDSQoY6iWawY7PVdhbPq3HuE9LTyvqlglrYikjlgjMAxQjbCfMQTu32z58Z6jwgYIgc3YKoY7rsBme85wvh0lixIshcRREhAdCJFgLEixIBEMWNMAhCEAmmdLVULgDzNWmF7bMfQGbnOZdM+OBXDYcHcWqt1ZbCerxRxbHL7x/XCUcR8RH63CZPGU7nwlXAYFsTiEpICdtwLgHJb5m/ZG2et13/ULDFcDhQd4pJ47Imyo5GR8ZW0RhhTpoo4AAdku7N5zu8lZQZjcZUVRfIS1Xp8rjsM1rTCM3ukkjlfKLVsWC1k00xVlpHLi4+k43WzdjzJPnOu6YwoCEbspyV0sxHbNPF9uX8rfCA745xuHfAC5j7XM1creui7R5eqKlgcxTAPBqjfEOzZv2XnoacY6FqF6rXzCbTgZWDZLftzInZxIbToRYkWEiEIQFhC8IDokWJAQxIpiQCEIhaAs4Xrb7fSONrnD03qqG2Q6AlQi+6o2twva+f2p3B87jgZFRwiKAqqqqNyqAqjsAgcg1f6Nq1Sqj4ohEsTsIbte1hc8x9OMzWrWp5w1Z/bHbqKbK3ApwZRwB/W6dM9na0TEYVaoHysPhbiOo8x1SuU3F/HZjWPpCwAjpHU2qZs4tyI+FuwwNUTJvDK8xFejc3mVeoDKdUiVq8atpzDXVpxrHUtl3HJmH+oz0ONENWza6JzI95uwfWcQ1nwoTGYxFFgtaooG+wDGaeOWMPyNXTXGS3fFO7vjsVv7vzld3mzivbsHQdix7SshAuUyPzfEuXlOxKwnnroax2xj6aH5w6/6GI8xPQAEhtj0nhIwTHBoSdCEIBCLCA6JFiQGmEJIqwISeEFWPrpuYcMj2REMJO2YbMWOBgNtBY+MBgPJBFmAI5HMSpV0XSbdtJ905eBlkmJIslWls6Uf+Drxdj2ACTUsDTXMKL82zMn2zAyJjIXO37Rsl55p1mrbWN0gf+5xI/lrMB6T0yct88s6Yq3xeL5mrUY/eNQlvWWZ2sRiv14yq4lrFcurzvKoksmW1TxrUMXhqqkArUUi+4m+49R3d89TYWutRVdc1YAjnY/WeSMMvvKN2e8XyHE+F56L6PdMGrRCVD76kq274x2c8j3yGmHTc7RDHCBWFyU3vlxEfKu1ssOW498tQgsIQgLEMWIYAgzk9pFS3yaEwxhcEc5DSEmbqkbZG/AwFaJePcSMwlKJFHrIzvhB5jQY4yO8BwEWIIsCLFtZHPUfSeS8RX2q9SpwZ3buaoSPWepNaK5p4TEODYhGseRtlPKR3mGeafGDd2H1vKRlnEPcL3377flK8lRYwbWem/JgTfdcEX7p2vVwGhiKdifZ1QEuTkGAJpn/b+KcVwgBIB7e8fozumAw5fCYc8TS902zVgNpTbndQe6VrXxuh4SptLfjuPbJWMxeiK20Eb7SgnqJF7TJtJWU8SJcQ3AMq4mWaUISQiQgLEMWIYD6PGSmR0ZIYTDTGsI4xohJeEiaSyNhAcJG2+SCRPvgPkbSRY14QFigxqxxga30iYjYwGJIzOwbDsBJ8gZ5iUZ/rnPSHStV2NHYg812R+MhPRjPO9elZVPPaHoYZ5qlQxq74rmFMZwouYJc+wGd/0Db9locLISOq7ZDz9ZwfCU8wed/DL+87nqahfD0hYkKNkjm1zkDzJNjy96RWuDatEU7Kg+yoHLh+XqJk2keFo7IA48T1ySobdsldVr5yyMrSDZzEs1FyhUsIgMIDjEMWNMCekMhHGCjIQMJNMYTHmMaEnbQIvGRtD4bciR3Xy8jHQH2kDyxIaghBAYPARrQFWOjVkloGgdM9QjABR89VR3AF/wDZ5TjWmMHsUaLZ7yL8Dmb/AEnZOmAg0sJTPzVSewhQB/VOb62YQpg8Le+bnI89kk3hWxoDDOWaFLdGonvd8yGGpZDLiAfEQzkXNG4bbKKN5JBtvscvznoHVbAexo0qZ3oov1uR7x9fGci6P9FmpiArD4AQwPMhgT1/D6cp2/RdMhQDmc8++R9tpOGQVY11kqiNqSRDTTOWWXKR0lk5gU9q2USSskINFMS0dBBmIQswtFhCyNhGESVpGYEVIZsOw+X9o8iNQ+/bmvof/aSkQEEjcSSMYQI4wx5iEQFEmUSuDLAOUIc26Th7XF4GhvyYtvyzDeiHwml9JdXZGEo5e6hYjkTcA27L+E3PGOMRpLEVd6UKWx1Co1x4lbgcrHnOXa54s1sS7b1sETK3uILZdWTHvhGXTB4Slc+fn/8AZmcDhDdctzA8swgeRaGwhZlWxu1+H2V2jOhYbV006yKfeHtEIPC3vhs/ugeEi1GOLM6qaIFHFswWwdFIysAQGVh4hT+KdAoL6n1mM0dhQNhj8QFj4C/pFwOlGdFYIBcXzJbfn1SN6aTG3pmwI1hMdUxtTmB2AfWYnHYuob/vH/CxX0tIuUWnjtbOCBmSB1k2EjbG0h8wP3fe9JgdD6LVkFR2cliTYm+V7DM58POZmlQVfhUD18ZMtqt1D/2i/wAjeA/OEWElXZ8Fa2cIkkSe3PISOpjtkgFD2giJaQYsZA9frIvSce+U5x6cm8B+cjOkE5N4D85VMgqSnyrX4RaGkENekgDXYOouBs/Dtc/4Zk5q1Y7NTDvyqqP570z/AFzapbG7Vzx0aRGGSESNpZRGwjRHtGLAAJT0zjTSpMQRtWOzyv8AaPUBc93ZL6iVsRo9XI2sxfPr6uyEOeDDVaeGYKpOIxDPUb3h+7pkgBi3ygKEt1k85qeitVnrk1CpKm6UQVPvIt2eoL7lNsuRYDjO5f8ADqZDAqCGFj1jlJKWCprbZUCwsLAZLy8hIS5vgdTjRxOGIUlUUmo1t7EqcvxDwBm5rgluMhkQR2gfrxmUrmR01jQr6Qf2dGqw3hGt22NvOVMFS2VUcgB5STWBv3apxeoi9wbaPkpklMWEpl21w9UdaYbHHfbumXxBmMprt1qS82BPYMz5CUaTiNmoUthFQfKAPAWklosJu5SQhCEHQhEhYSPED3T4ySBF8pBFDhIKkmBkNUzJ0RjdLNakzDevvjtU7Q9JtyNcAzV66BlZTxBEzOga23h6LHfsLftCgHzlsFfL1GRjHEW8a7TRiiaNEc8QQJEEcTGgxUECRREaOkNZoFeqc4+mJGM5MIGG0t71bDpy26neAEH9ZlsjKU3O1iX/AIaar3sWJ+kundMr23nUUcU2+VtBrtVyfsqT3kgfUyTGtvhqqtzWb7o9SfpKz2Wz4xrYYkLxJu5RCEINHxIQhIhCEDH1B7zDrletLGLyc9YB+n0lerMa6MelUmZDV9rUynJm8C20PWYyrlLmiKliw55+X9owvJ5J/Vmw0GkSGTGbOcloySiRsIAJMgjKYksBGMqVWvJaryBRAcgj2iXkeKqbKM3IE+UgjD6PO01d/tOQOxQFHpL4mP0StqS9dye8mW2aZOhjtJtvljVMfuqh51D5Kv5yhpR8jMhqkb0Cebt9B9JGHseT1ZqESE3c4hEvCBJEiwgJAQhAo4/4l7PrKtSEJjl26MPVj8SZNotjtd31hCVx7Wy9WepyZjCE3cpyxHhCSH04rwhArPGpCEBRKesBtQa3MeF4sJFTO1HA/wCXT+6PSS1N0ITF0RgdMsbGZvVMf4ZPvP8A1mEI8fsjy+rMRDCE3c5IQhA//9k="
                    alt="Ticket Preview"
                    className=" object-cover"
                />
            </div>
            <div className="flex mt-[50px] mx-[10px] w-[300px]  bg-white ">
                <div className="flex flex-col w-2/5 border border-[#666666]">
                    <p className="ml-[5px] mt-[5px] text-[25px]">식별번호</p>
                    <p className="ml-[5px] mt-[5px] text-[25px]">이름</p>
                    <p className="ml-[5px] mt-[5px] text-[25px]">생년월일</p>
                    <p className="ml-[5px] mt-[5px] text-[25px]">일치율</p>
                    <p className="ml-[5px] mt-[5px] text-[25px]">좌석</p>
                </div>
                <div className="flex flex-col w-3/5 border border-[#666666]">
                    <p className="ml-[5px] mt-[5px] text-[25px]">{socketMessage?.data.ticketId}</p>
                    <p className="ml-[5px] mt-[5px] text-[25px]">{socketMessage?.name}</p>
                    <p className="ml-[5px] mt-[5px] text-[25px]">{socketMessage?.birth}</p>
                    <p className="ml-[5px] mt-[5px] text-[25px]">{socketMessage?.data.similarity}</p>
                    <p className="ml-[5px] mt-[5px] text-[25px]">{socketMessage?.seatLoc}</p>
                </div>
            </div>
        </div>
    );
};

export default ManagerCheckPage;
